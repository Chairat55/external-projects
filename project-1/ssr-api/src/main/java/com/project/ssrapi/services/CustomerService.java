package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.customer.ReqCreateCustomerDTO;
import com.example.shinsiri.dtos.req.customer.ReqSearchCustomerDTO;
import com.example.shinsiri.dtos.res.customer.ResCustomerDTO;
import com.example.shinsiri.dtos.res.customer.ResSearchCustomerDTO;
import com.example.shinsiri.entities.Customer;
import com.example.shinsiri.entities.CustomerTag;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.CustomerImageRepository;
import com.example.shinsiri.repositories.CustomerRepository;
import com.example.shinsiri.repositories.CustomerTagRepository;
import com.example.shinsiri.repositories.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final String CUSTOMER_TYPE = "CUSTOMER";
    private static final String COMPANY_TYPE = "COMPANY";

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerImageRepository customerImageRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CustomerTagRepository customerTagRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public ResCustomerDTO getCustomerById(int customerId) {
        Optional<Customer> cus = customerRepository.findById(customerId);
        if (cus.isEmpty()) {
            throw new BadRequestException("customerId: " + customerId + " ไม่มีในระบบ");
        }

        ResCustomerDTO resDto = new ResCustomerDTO();
        modelMapper.map(cus.get(), resDto);

        resDto.setCustomerImages(customerImageRepository.findAllByCustomerId(customerId));
        resDto.setTags(tagRepository.findAllByCustomerId(customerId));
        return resDto;
    }

    public ResSearchCustomerDTO searchCustomer(ReqSearchCustomerDTO dto) {
        int pageNo = dto.getPageNo() == null ? 1 : dto.getPageNo();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        String sql = "SELECT * FROM customers WHERE 1=1 ";
        String sqlCount = "SELECT COUNT(id) FROM customers WHERE 1=1 ";
        String sqlCountAll = "SELECT COUNT(id) FROM customers ";

        if (dto.getType() != null && !dto.getType().equals("")) {
            sql += "    AND type = :type ";
            sqlCount += "    AND type = :type ";
        }

        Query query = em.createNativeQuery(sql, Customer.class);
        Query queryCount = em.createNativeQuery(sqlCount);
        Query queryCountAll = em.createNativeQuery(sqlCountAll);

        if (dto.getType() != null && !dto.getType().equals("")) {
            query.setParameter("type", dto.getType());
            queryCount.setParameter("type", dto.getType());
        }

        query.setFirstResult((pageNo - 1) * pageSize);
        query.setMaxResults(pageSize);

        int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();
        int totalAll = ((BigInteger) queryCountAll.getSingleResult()).intValue();

        ResSearchCustomerDTO resDto = new ResSearchCustomerDTO();
        resDto.setPageNo(pageNo);
        resDto.setPageSize(pageSize);
        resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
        resDto.setTotalItems(totalItems);
        resDto.setTotalAll(totalAll);
        resDto.setItems(query.getResultList());

        return resDto;
    }

    public Customer createCustomer(ReqCreateCustomerDTO dto, int userId) {
        validateTel(dto.getContactTel());
        checkCustomerType(dto.getType());
        checkCustomerDuplicate(dto.getFullName());

        Customer customer = new Customer();
        modelMapper.map(dto, customer);
        customer.setUserId(userId);

        customerRepository.saveAndFlush(customer);
        updateTag(customer.getId(), dto.getTagIds(), "CREATE");

        return customer;
    }

    public Customer updateCustomer(int customerId, ReqCreateCustomerDTO dto, int userId) {
        validateTel(dto.getContactTel());
        checkCustomerType(dto.getType());

        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            if (!customer.getFullName().equals(dto.getFullName())) {
                checkCustomerDuplicate(dto.getFullName());

                modelMapper.map(dto, customer);
                customer.setUserId(userId);

                customerRepository.saveAndFlush(customer);
                updateTag(customer.getId(), dto.getTagIds(), "UPDATE");

            }
        } else {
            throw new BadRequestException("Customer id: " + customerId + " ไม่มีในระบบ");
        }
        return customer;
    }

    public void checkCustomerDuplicate(String fullName) {
        Customer customer = customerRepository.findOneByFullName(fullName);
        if (customer != null) throw new BadRequestException("Customer มีอยู่แล้วในระบบ");
    }

    private void checkCustomerType(String type) {
        if (!type.equals(CUSTOMER_TYPE) && !type.equals(COMPANY_TYPE)) {
            throw new BadRequestException("Type ต้องเป็น CUSTOMER หรือ COMPANY เท่านั้น");
        }
    }

    private void validateTel(String contactTel) {
        if (contactTel.length() != 9 && contactTel.length() != 10) {
            throw new BadRequestException("ContactTel ต้อง 9 หรือ 10 ตัวอักษร เท่านั้น");
        }
    }

    private void updateTag(int customerId, List<Integer> tagIds, String type) {
        if (tagIds.size() > 0) {
            List<CustomerTag> customerTags = new ArrayList<>();
            for (Integer tagId : tagIds) {
                CustomerTag customerTag = new CustomerTag();
                customerTag.setCustomerId(customerId);
                customerTag.setTagId(tagId);

                customerTags.add(customerTag);
            }

            if (type.equals("UPDATE")) {
                customerTagRepository.deleteAll(customerTagRepository.findAllByCustomerId(customerId));
                customerTagRepository.saveAll(customerTags);
            } else {
                customerTagRepository.saveAll(customerTags);
            }
        }

    }

}

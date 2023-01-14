package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.crm.ReqCreateCrmDTO;
import com.example.shinsiri.dtos.req.crm.ReqSearchCrmDTO;
import com.example.shinsiri.dtos.res.crm.ResCrmDTO;
import com.example.shinsiri.dtos.res.crm.ResSearchCrmDTO;
import com.example.shinsiri.entities.Crm;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.CrmRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrmService {

    @Autowired
    private CrmRepository crmRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public ResSearchCrmDTO searchCrm(ReqSearchCrmDTO dto) {
        int pageNo = dto.getPageNo() == null ? 1 : dto.getPageNo();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        String sql = "SELECT * FROM crms WHERE 1=1 ";
        String sqlCount = "SELECT COUNT(id) FROM crms WHERE 1=1 ";
        String sqlCountAll = "SELECT COUNT(id) FROM crms ";

        if (dto.getFullName() != null && !dto.getFullName().equals("")) {
            sql += "    AND full_name LIKE :fullName ";
            sqlCount += "    AND full_name LIKE :fullName ";
        }

        Query query = em.createNativeQuery(sql, Crm.class);
        Query queryCount = em.createNativeQuery(sqlCount);
        Query queryCountAll = em.createNativeQuery(sqlCountAll);

        if (dto.getFullName() != null && !dto.getFullName().equals("")) {
            query.setParameter("fullName", "%" + dto.getFullName() + "%");
            queryCount.setParameter("fullName", "%" + dto.getFullName() + "%");
        }

        query.setFirstResult((pageNo - 1) * pageSize);
        query.setMaxResults(pageSize);

        int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();
        int totalAll = ((BigInteger) queryCountAll.getSingleResult()).intValue();

        List<ResCrmDTO> mapResult = mapCrmListToResCrmDTOList(query.getResultList());

        ResSearchCrmDTO resDto = new ResSearchCrmDTO();
        resDto.setPageNo(pageNo);
        resDto.setPageSize(pageSize);
        resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
        resDto.setTotalItems(totalItems);
        resDto.setTotalAll(totalAll);
        resDto.setItems(mapResult);

        return resDto;
    }

    private List<ResCrmDTO> mapCrmListToResCrmDTOList(List<Crm> crms) {
        List<ResCrmDTO> resDtoList = new ArrayList<>();

        for (Crm crm : crms) {
            ResCrmDTO resDto = new ResCrmDTO();
            modelMapper.map(crm, resDto);

            if (!crm.getHashtags().equals("")) {
                resDto.setHashtagList(List.of(crm.getHashtags().split("\\|")));
            }

            resDtoList.add(resDto);
        }

        return resDtoList;
    }

    public Crm createCrm(ReqCreateCrmDTO dto, int userId) {
        checkCrmDuplicate(dto.getFullName());

        Crm crm = new Crm();
        modelMapper.map(dto, crm);
        crm.setUserId(userId);
        if (dto.getHashtagList().size() > 0) {
            crm.setHashtags(String.join("\\|", dto.getHashtagList()));
        } else {
            crm.setHashtags("");
        }

        crmRepository.saveAndFlush(crm);
        return crm;
    }

    public Map<String, String> deleteCrm(int crmId) {
        Crm crm = crmRepository.findById(crmId).orElse(null);
        if (crm != null) {
            crmRepository.delete(crm);
        } else {
            throw new BadRequestException("Crm id: " + crmId + " ไม่มีอยู่ในระบบ");
        }
        return new HashMap<>() {{
            put("Message", "ลบสำเร็จ");
        }};
    }

    public void checkCrmDuplicate(String fullName) {
        Crm crm = crmRepository.findOneByFullName(fullName);
        if (crm != null) throw new BadRequestException("CRM มีอยู่แล้วในระบบ");
    }

}

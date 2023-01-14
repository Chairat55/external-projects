package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.customer.ReqCreateCustomerDTO;
import com.example.shinsiri.dtos.req.customer.ReqSearchCustomerDTO;
import com.example.shinsiri.dtos.res.customer.ResCustomerDTO;
import com.example.shinsiri.dtos.res.customer.ResSearchCustomerDTO;
import com.example.shinsiri.entities.Customer;
import com.example.shinsiri.entities.User;
import com.example.shinsiri.repositories.CustomerRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private CustomerService customerService;


    @GetMapping("")
    public List<Customer> getCustomers() {
        logger.info("[GET] /customers");
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResCustomerDTO getCustomerById(
            @PathVariable int id
    ) {
        logger.info("[GET] /customers/{}", id);
        return customerService.getCustomerById(id);
    }

    @PostMapping("/search")
    public ResSearchCustomerDTO searchCustomer(
            @RequestBody ReqSearchCustomerDTO dto
    ) {
        logger.info("[POST] /customers/search with dto: {}", dto);
        return customerService.searchCustomer(dto);
    }

    @PostMapping("")
    public Customer createCustomer(
            Authentication authentication,
            @Valid @RequestBody ReqCreateCustomerDTO dto
    ) {
        logger.info("[POST] /customers with dto: {}", dto);
        User user = authenticationService.checkAuthentication(authentication);
        return customerService.createCustomer(dto, user.getId());
    }

    @PutMapping("/{id}")
    public Customer updateCustomer(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqCreateCustomerDTO dto
    ) {
        logger.info("[PUT] /customers/{} with dto: {}", id, dto);
        User user = authenticationService.checkAuthentication(authentication);
        return customerService.updateCustomer(id, dto, user.getId());
    }

}

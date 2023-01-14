package com.project.ssrapi.dtos.res.customer;

import com.example.shinsiri.entities.Customer;
import lombok.Data;

import java.util.List;

@Data
public class ResSearchCustomerDTO {

    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    int totalAll;
    List<Customer> items;

}

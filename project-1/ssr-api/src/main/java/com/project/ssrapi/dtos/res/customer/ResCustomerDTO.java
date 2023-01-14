package com.project.ssrapi.dtos.res.customer;

import com.example.shinsiri.entities.Customer;
import com.example.shinsiri.entities.CustomerImage;
import com.example.shinsiri.entities.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ResCustomerDTO extends Customer {

    List<CustomerImage> customerImages;
    List<Tag> tags;

}

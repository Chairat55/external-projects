package com.project.ssrapi.dtos.res.order;

import com.example.shinsiri.entities.*;
import lombok.Data;

import java.util.List;

@Data
public class ResOrderDTO extends Order {

    Customer customer;
    User user;
    List<OrderDetail> orderDetails;
    List<OrderImage> orderImages;

}

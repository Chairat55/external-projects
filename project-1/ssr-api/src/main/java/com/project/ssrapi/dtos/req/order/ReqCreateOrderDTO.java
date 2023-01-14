package com.project.ssrapi.dtos.req.order;

import com.example.shinsiri.entities.OrderDetail;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReqCreateOrderDTO {

    @NotNull(message = "customerId is required")
    int customerId;

    @NotNull(message = "priority is required")
    int priority;

    @NotBlank(message = "contactName is required")
    String contactName;

    @NotBlank(message = "contactTel is required")
    String contactTel;

    String contactEmail;

    @NotBlank(message = "deliveryAddress is required")
    String deliveryAddress;

    @NotBlank(message = "deliveryProvince is required")
    String deliveryProvince;

    @NotBlank(message = "deliveryDistrict is required")
    String deliveryDistrict;

    @NotBlank(message = "deliverySubdistrict is required")
    String deliverySubdistrict;

    @NotBlank(message = "deliveryZipcode is required")
    String deliveryZipcode;

    @NotBlank(message = "paymentTerm is required")
    String paymentTerm;

    @NotBlank(message = "paymentCredit is required")
    String paymentCredit;

    @NotBlank(message = "paymentType is required")
    String paymentType;

    String remark;

    @NotEmpty(message = "orderDetails is required")
    List<OrderDetail> orderDetails;

    String deliveryType;

    LocalDateTime deliveryDate;

}

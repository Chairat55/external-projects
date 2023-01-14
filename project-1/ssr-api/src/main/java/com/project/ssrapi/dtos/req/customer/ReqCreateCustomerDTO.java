package com.project.ssrapi.dtos.req.customer;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class ReqCreateCustomerDTO {

    @NotBlank(message = "type is required")
    String type;

    @NotBlank(message = "fullName is required")
    String fullName;

    @NotNull(message = "birthDate is required")
    Date birthDate;

    String companyName;

    @NotBlank(message = "businessName is required")
    String businessName;

    @NotNull(message = "businessDate is required")
    Date businessDate;

    @NotBlank(message = "businessType is required")
    String businessType;

    @NotBlank(message = "address is required")
    String address;

    @NotBlank(message = "province is required")
    String province;

    @NotBlank(message = "district is required")
    String district;

    @NotBlank(message = "subdistrict is required")
    String subdistrict;

    @NotBlank(message = "zipcode is required")
    String zipcode;

    @NotBlank(message = "addressDetail is required")
    String addressDetail;

    @NotBlank(message = "contactTel is required")
    String contactTel;

    String contactEmail;
    String contactDetail;
    List<Integer> tagIds;

}

package com.project.ssrapi.dtos.req.crm;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class ReqCreateCrmDTO {

    @NotBlank(message = "fullName is required")
    String fullName;

    @NotNull(message = "birthDate is required")
    Date birthDate;

    @NotBlank(message = "businessName is required")
    String businessName;

    @NotNull(message = "businessDate is required")
    Date businessDate;

    @NotNull(message = "hashtagList is required")
    List<String> hashtagList;

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

    @NotBlank(message = "contactTel is required")
    String contactTel;

    String contactEmail;
    String contactDetail;

    @NotBlank(message = "expectResult is required")
    String expectResult;

    String expectResultDetail;

    @NotNull(message = "expectResultDate is required")
    Date expectResultDate;

    String expectResultTime;

    @NotBlank(message = "actualResult is required")
    String actualResult;

    String actualResultDetail;

}

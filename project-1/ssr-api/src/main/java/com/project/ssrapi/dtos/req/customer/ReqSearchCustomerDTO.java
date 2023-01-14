package com.project.ssrapi.dtos.req.customer;

import lombok.Data;

@Data
public class ReqSearchCustomerDTO {

    Integer pageNo;
    Integer pageSize;
    String type;

}

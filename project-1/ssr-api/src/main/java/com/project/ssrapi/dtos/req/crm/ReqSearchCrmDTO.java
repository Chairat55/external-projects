package com.project.ssrapi.dtos.req.crm;

import lombok.Data;

@Data
public class ReqSearchCrmDTO {

    Integer pageNo;
    Integer pageSize;
    String fullName;

}

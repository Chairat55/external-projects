package com.project.ssrapi.dtos.req.product;

import lombok.Data;

@Data
public class ReqSearchProductDTO {

    Integer pageNo;
    Integer pageSize;
    String type;
    String name;

}

package com.project.ssrapi.dtos.req.tag;

import lombok.Data;

@Data
public class ReqSearchTagDTO {

    Integer pageNo;
    Integer pageSize;
    String name;

}

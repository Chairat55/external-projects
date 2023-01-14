package com.project.ssrapi.dtos.req.user;

import lombok.Data;

@Data
public class ReqSearchUserDTO {

    Integer pageNo;
    Integer pageSize;
    String type;

}

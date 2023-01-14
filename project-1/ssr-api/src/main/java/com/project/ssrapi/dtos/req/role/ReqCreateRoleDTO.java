package com.project.ssrapi.dtos.req.role;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqCreateRoleDTO {

    @NotNull(message = "name is required")
    String name;

}

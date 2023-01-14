package com.project.ssrapi.dtos.req.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqCreateUserDTO {

    @NotBlank(message = "username is required")
    String username;

    @NotBlank(message = "email is required")
    String email;

    @NotBlank(message = "password is required")
    String password;

    @NotBlank(message = "fullName is required")
    String fullName;

    @NotBlank(message = "tel is required")
    String tel;

    String lineId;
    String otherContact;

    @NotNull(message = "roleId is required")
    int roleId;

}

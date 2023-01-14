package com.project.ssrapi.dtos.req.file;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqCreateFileDTO {

    @NotBlank(message = "name is required")
    String name;

    @NotBlank(message = "type is required")
    String type;

}

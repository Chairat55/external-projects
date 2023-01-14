package com.project.ssrapi.dtos.req.tag;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqCreateTagDTO {

    @NotBlank(message = "name is required")
    String name;

}

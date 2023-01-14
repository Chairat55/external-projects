package com.project.ssrapi.dtos.req.product;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqCreateProductDTO {

    @NotBlank(message = "name is required")
    String name;

    @NotBlank(message = "code is required")
    String code;

    @NotBlank(message = "type is required")
    String type;

    @NotNull(message = "stock is required")
    Integer stock;

    @NotNull(message = "price is required")
    Integer price;

}

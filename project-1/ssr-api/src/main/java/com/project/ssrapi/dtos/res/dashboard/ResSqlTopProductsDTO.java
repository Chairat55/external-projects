package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResSqlTopProductsDTO {
    private String name;
    private String type;
    private BigDecimal total;
}

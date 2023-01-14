package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResSqlSumProductTypeDTO {
    private String producttype;
    private BigDecimal sum;
}

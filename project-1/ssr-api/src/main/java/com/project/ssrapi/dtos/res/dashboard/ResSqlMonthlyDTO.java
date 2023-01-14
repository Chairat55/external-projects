package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResSqlMonthlyDTO {
    private String month;
    private BigDecimal total;
}

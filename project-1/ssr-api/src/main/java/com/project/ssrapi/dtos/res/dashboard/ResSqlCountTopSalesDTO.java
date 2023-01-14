package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class ResSqlCountTopSalesDTO {
    private BigInteger count;
    private BigDecimal sum;
}

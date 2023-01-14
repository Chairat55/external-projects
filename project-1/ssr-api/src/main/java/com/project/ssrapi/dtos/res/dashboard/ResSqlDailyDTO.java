package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ResSqlDailyDTO {
    private String day;
    private BigInteger count;
}

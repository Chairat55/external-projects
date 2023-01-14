package com.project.ssrapi.dtos.res.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class ResSqlTopSalesDTO {
    @JsonProperty("userId")
    private int userid;

    @JsonProperty("imagePath")
    private String imagepath;

    @JsonProperty("fullName")
    private String fullname;

    private String tel;
    private BigDecimal total;
    private double percent;

    @JsonProperty("countOrder")
    private BigInteger countorder;
}

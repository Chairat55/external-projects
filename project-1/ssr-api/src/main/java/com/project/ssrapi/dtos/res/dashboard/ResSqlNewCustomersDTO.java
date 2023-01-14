package com.project.ssrapi.dtos.res.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResSqlNewCustomersDTO {
    @JsonProperty("fullName")
    private String fullname;
}

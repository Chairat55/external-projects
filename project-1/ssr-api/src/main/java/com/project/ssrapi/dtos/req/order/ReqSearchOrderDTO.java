package com.project.ssrapi.dtos.req.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReqSearchOrderDTO {

    Integer pageNo;
    Integer pageSize;
    String search;
    String province;
    String productName;
    List<String> status;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime orderStartDate;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime orderEndDate;
    String customerType;
    Integer userId;
    Integer customerId;

}

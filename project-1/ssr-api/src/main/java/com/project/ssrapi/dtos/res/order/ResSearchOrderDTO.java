package com.project.ssrapi.dtos.res.order;

import lombok.Data;

import java.util.List;

@Data
public class ResSearchOrderDTO {

    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    int totalAll;
    List<ResOrderDTO> items;

}

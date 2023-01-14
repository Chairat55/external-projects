package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class ResTopSalesDTO {
    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    List<ResSqlTopSalesDTO> items;
}

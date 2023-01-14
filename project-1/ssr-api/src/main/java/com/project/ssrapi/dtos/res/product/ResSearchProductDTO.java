package com.project.ssrapi.dtos.res.product;

import lombok.Data;

import java.util.List;

@Data
public class ResSearchProductDTO {

    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    int totalAll;
    int totalInStock;
    int totalOutOfStock;
    List<ResProductDTO> items;

}

package com.project.ssrapi.dtos.res.crm;

import lombok.Data;

import java.util.List;

@Data
public class ResSearchCrmDTO {

    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    int totalAll;
    List<ResCrmDTO> items;

}

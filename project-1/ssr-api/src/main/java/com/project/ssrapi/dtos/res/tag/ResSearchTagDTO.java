package com.project.ssrapi.dtos.res.tag;

import com.example.shinsiri.entities.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ResSearchTagDTO {

    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    int totalAll;
    List<Tag> items;

}

package com.project.ssrapi.dtos.res.user;

import com.example.shinsiri.entities.User;
import lombok.Data;

import java.util.List;

@Data
public class ResSearchUserDTO {

    int pageNo;
    int pageSize;
    int totalItems;
    int totalPages;
    int totalAll;
    List<User> items;

}

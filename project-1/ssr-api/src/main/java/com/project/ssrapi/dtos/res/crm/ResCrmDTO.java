package com.project.ssrapi.dtos.res.crm;

import com.example.shinsiri.entities.Crm;
import lombok.Data;

import java.util.List;

@Data
public class ResCrmDTO extends Crm {

    List<String> hashtagList;

}

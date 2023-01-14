package com.project.ssrapi.dtos.res.order;

import lombok.Data;

@Data
public class ResOrderCalculateDTO {

    double total;
    double discount;
    double net;

}

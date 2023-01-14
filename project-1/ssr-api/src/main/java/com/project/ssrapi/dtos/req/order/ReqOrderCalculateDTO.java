package com.project.ssrapi.dtos.req.order;

import com.example.shinsiri.entities.OrderDetail;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ReqOrderCalculateDTO {

    @NotEmpty(message = "orderDetails is required")
    List<OrderDetail> orderDetails;

}

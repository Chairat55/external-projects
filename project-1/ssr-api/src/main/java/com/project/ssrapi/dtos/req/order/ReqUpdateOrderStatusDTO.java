package com.project.ssrapi.dtos.req.order;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class ReqUpdateOrderStatusDTO {

    @NotBlank(message = "status is required")
    String status;

    String rejectRemark;

    LocalDateTime rejectDueDate;

}

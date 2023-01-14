package com.project.ssrapi.dtos.req.upload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqUploadProductImageDTO {

    @NotNull(message = "productId is required")
    private Integer productId;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "imageName is required")
    private String imageName;

    @NotBlank(message = "imageData is required")
    private String imageData; //BASE 64

    public ReqUploadProductImageDTO(String imageName, String imageData) {
        this.imageName = imageName;
        this.imageData = imageData;
    }

}

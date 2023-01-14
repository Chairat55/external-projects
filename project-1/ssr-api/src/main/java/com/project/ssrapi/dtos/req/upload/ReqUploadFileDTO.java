package com.project.ssrapi.dtos.req.upload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqUploadFileDTO {

    @NotNull(message = "fileId is required")
    private Integer fileId;

    @NotBlank(message = "fileName is required")
    private String fileName;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "fileData is required")
    private String fileData; //BASE 64

    public ReqUploadFileDTO(String fileName, String fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

}

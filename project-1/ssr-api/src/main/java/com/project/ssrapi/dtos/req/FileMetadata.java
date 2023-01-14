package com.project.ssrapi.dtos.req;

import lombok.Data;

@Data
public class FileMetadata {

    private String path;
    private String fileName;
    private String fileBase64;

}

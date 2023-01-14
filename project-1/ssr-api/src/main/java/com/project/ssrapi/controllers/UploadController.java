package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.FileMetadata;
import com.example.shinsiri.dtos.req.upload.*;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UploadService uploadService;

    @PostMapping("/customerImage")
    private FileMetadata uploadCustomerImage(
            Authentication authentication,
            @Valid @RequestBody ReqUploadCustomerImageDTO dto
    ) {
        logger.info("[POST] /upload/customerImage with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return uploadService.uploadCustomerImage(dto);
    }

    @PostMapping("/userImage")
    private FileMetadata uploadUserImage(
            Authentication authentication,
            @Valid @RequestBody ReqUploadUserImageDTO dto
    ) {
        logger.info("[POST] /upload/userImage with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return uploadService.uploadUserImage(dto);
    }

    @PostMapping("/productImage")
    private FileMetadata productImage(
            Authentication authentication,
            @Valid @RequestBody ReqUploadProductImageDTO dto
    ) {
        logger.info("[POST] /upload/productImage with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return uploadService.uploadProductImage(dto);
    }

    @PostMapping("/orderImage")
    private FileMetadata orderImage(
            Authentication authentication,
            @Valid @RequestBody ReqUploadOrderImageDTO dto
    ) {
        logger.info("[POST] /upload/orderImage with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return uploadService.uploadOrderImage(dto);
    }

    @PostMapping("/file")
    private FileMetadata file(
            Authentication authentication,
            @Valid @RequestBody ReqUploadFileDTO dto
    ) {
        logger.info("[POST] /upload/file with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return uploadService.uploadFile(dto);
    }

}

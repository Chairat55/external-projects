package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.file.ReqCreateFileDTO;
import com.example.shinsiri.entities.File;
import com.example.shinsiri.repositories.FileRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private FileService fileService;


    @GetMapping("")
    public List<File> getFiles() {
        logger.info("[GET] /files");
        return fileRepository.findAll();
    }

    @GetMapping("/{id}")
    public File getFileById(
            @PathVariable int id
    ) {
        logger.info("[GET] /files/{}", id);
        return fileService.getFileById(id);
    }

    @PostMapping("")
    public File createFile(
            Authentication authentication,
            @Valid @RequestBody ReqCreateFileDTO dto
    ) {
        logger.info("[POST] /files with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return fileService.createFile(dto);
    }

    @PutMapping("/{id}")
    public File updateFile(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqCreateFileDTO dto
    ) {
        logger.info("[PUT] /files/{} with dto: {}", id, dto);
        authenticationService.checkAuthentication(authentication);
        return fileService.updateFile(id, dto);
    }

}

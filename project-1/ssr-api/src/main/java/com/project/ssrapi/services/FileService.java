package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.file.ReqCreateFileDTO;
import com.example.shinsiri.entities.File;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.FileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public File getFileById(int fileId) {
        Optional<File> file = fileRepository.findById(fileId);
        if (file.isEmpty()) {
            throw new BadRequestException("fileId: " + fileId + " ไม่มีในระบบ");
        }

        return file.get();
    }

    public File createFile(ReqCreateFileDTO dto) {
        checkFileDuplicate(dto.getName());

        File file = new File();
        modelMapper.map(dto, file);
        file.setType(dto.getType().toUpperCase());

        fileRepository.saveAndFlush(file);
        return file;
    }

    public File updateFile(int fileId, ReqCreateFileDTO dto) {
        File file = fileRepository.findById(fileId).orElse(null);
        if (file != null) {
            if (!file.getName().equals(dto.getName())) {
                checkFileDuplicate(dto.getName());

                modelMapper.map(dto, file);
                file.setType(dto.getType().toUpperCase());

                fileRepository.saveAndFlush(file);
            }
        } else {
            throw new BadRequestException("File id: " + fileId + " ไม่มีในระบบ");
        }
        return file;
    }

    public void checkFileDuplicate(String name) {
        File file = fileRepository.findOneByName(name);
        if (file != null) throw new BadRequestException("File มีอยู่แล้วในระบบ");
    }

}

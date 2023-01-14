package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.tag.ReqCreateTagDTO;
import com.example.shinsiri.dtos.req.tag.ReqSearchTagDTO;
import com.example.shinsiri.dtos.res.tag.ResSearchTagDTO;
import com.example.shinsiri.entities.Tag;
import com.example.shinsiri.repositories.TagRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TagService tagService;


    @GetMapping("")
    public List<Tag> getTags() {
        logger.info("[GET] /tags");
        return tagRepository.findAll();
    }

    @GetMapping("/{id}")
    public Tag getTagById(
            @PathVariable int id
    ) {
        logger.info("[GET] /tags/{}", id);
        return tagService.getTagById(id);
    }

    @PostMapping("/search")
    public ResSearchTagDTO searchTag(
            @RequestBody ReqSearchTagDTO dto
    ) {
        logger.info("[POST] /tags/search with dto: {}", dto);
        return tagService.searchTag(dto);
    }

    @PostMapping("")
    public Tag createTag(
            Authentication authentication,
            @Valid @RequestBody ReqCreateTagDTO dto
    ) {
        logger.info("[POST] /tags with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return tagService.createTag(dto);
    }

    @PutMapping("/{id}")
    public Tag updateTag(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqCreateTagDTO dto
    ) {
        logger.info("[PUT] /tags/{} with dto: {}", id, dto);
        authenticationService.checkAuthentication(authentication);
        return tagService.updateTag(id, dto);
    }

}

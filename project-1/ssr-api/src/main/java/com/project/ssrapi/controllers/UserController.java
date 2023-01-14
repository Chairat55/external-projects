package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.user.ReqCreateUserDTO;
import com.example.shinsiri.dtos.req.user.ReqSearchUserDTO;
import com.example.shinsiri.dtos.res.user.ResSearchUserDTO;
import com.example.shinsiri.dtos.res.user.ResUserDTO;
import com.example.shinsiri.entities.User;
import com.example.shinsiri.repositories.UserRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public User getProfile(
            Authentication authentication
    ) {
        logger.info("[GET] /users/me");
        authenticationService.checkAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        return userService.getUserById(user.getId());
    }

    @GetMapping("")
    public List<User> getUsers(
            Authentication authentication
    ) {
        logger.info("[GET] /users");
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResUserDTO getUserById(
            @PathVariable int id
    ) {
        logger.info("[GET] /users/{}", id);
        return userService.getUserById(id);
    }

    @PostMapping("/search")
    public ResSearchUserDTO searchUser(
            @RequestBody ReqSearchUserDTO dto
    ) {
        logger.info("[POST] /users/search with dto: {}", dto);
        return userService.searchUser(dto);
    }

    @PostMapping("")
    public User createUser(
            Authentication authentication,
            @Valid @RequestBody ReqCreateUserDTO dto
    ) {
        logger.info("[POST] /users with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return userService.createUser(dto);
    }

    @PutMapping("/{id}")
    public User updateUser(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqCreateUserDTO dto
    ) {
        logger.info("[PUT] /users/{} with dto: {}", id, dto);
        authenticationService.checkAuthentication(authentication);
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteUserById(
            @PathVariable int id
    ) {
        logger.info("[DELETE] /users/{}", id);
        return userService.deleteUserById(id);
    }

}

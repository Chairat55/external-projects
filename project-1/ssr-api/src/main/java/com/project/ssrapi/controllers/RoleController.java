package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.role.ReqCreateRoleDTO;
import com.example.shinsiri.entities.Role;
import com.example.shinsiri.repositories.RoleRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private RoleService roleService;


    @GetMapping("")
    public List<Role> getRoles() {
        logger.info("[GET] /roles");
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Role getRoleById(
            @PathVariable int id
    ) {
        logger.info("[GET] /roles/{}", id);
        return roleRepository.findById(id).orElse(null);
    }

    @PostMapping("")
    public Role createRole(
            Authentication authentication,
            @Valid @RequestBody ReqCreateRoleDTO dto
    ) {
        logger.info("[POST] /roles with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return roleService.createRole(dto);
    }

}

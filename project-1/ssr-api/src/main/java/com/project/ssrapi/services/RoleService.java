package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.role.ReqCreateRoleDTO;
import com.example.shinsiri.entities.Role;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role createRole(ReqCreateRoleDTO dto) {
        checkRoleDuplicate(dto.getName());

        Role role = new Role();
        role.setName(dto.getName());

        roleRepository.saveAndFlush(role);
        return role;
    }

    public void checkRoleDuplicate(String name) {
        Role role = roleRepository.findOneByName(name);
        if (role != null) throw new BadRequestException("Role มีอยู่แล้วในระบบ");
    }

}

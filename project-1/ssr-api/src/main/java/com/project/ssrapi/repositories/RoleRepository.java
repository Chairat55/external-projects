package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findOneByName(String name);

}

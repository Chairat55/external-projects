package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.Crm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmRepository extends JpaRepository<Crm, Integer> {

    Crm findOneByFullName(String fullName);

}

package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.CustomerTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerTagRepository extends JpaRepository<CustomerTag, Integer> {

    List<CustomerTag> findAllByCustomerId(int customerId);

}

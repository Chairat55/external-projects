package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.CustomerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerImageRepository extends JpaRepository<CustomerImage, Integer> {

    List<CustomerImage> findAllByCustomerId(int customerId);

    CustomerImage findOneByCustomerIdAndType(int customerId, String type);

}

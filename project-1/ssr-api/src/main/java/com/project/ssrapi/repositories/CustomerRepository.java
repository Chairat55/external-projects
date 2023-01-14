package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Customer findOneByFullName(String fullName);

    @Query("SELECT c FROM Customer c WHERE c.id IN :customerIds")
    List<Customer> findAllByCustomerIds(List<Integer> customerIds);

}

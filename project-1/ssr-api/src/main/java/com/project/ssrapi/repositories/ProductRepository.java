package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Product findOneByName(String name);

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findAllByIds(List<Integer> productIds);

}

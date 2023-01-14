package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Order findOneById(int id);

}

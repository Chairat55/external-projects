package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findAllByOrderId(int orderId);

    @Query("SELECT od FROM OrderDetail od WHERE od.orderId IN :orderIds")
    List<OrderDetail> findAllByOrderIds(List<Integer> orderIds);

}

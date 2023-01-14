package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.OrderImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderImageRepository extends JpaRepository<OrderImage, Integer> {

    List<OrderImage> findAllByOrderId(int orderId);

    OrderImage findOneByOrderIdAndType(int orderId, String type);

    @Query("SELECT oi FROM OrderImage oi WHERE oi.orderId IN :orderIds")
    List<OrderImage> findAllByOrderIds(List<Integer> orderIds);

}

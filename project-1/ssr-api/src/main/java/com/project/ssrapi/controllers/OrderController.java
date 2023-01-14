package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.order.ReqCreateOrderDTO;
import com.example.shinsiri.dtos.req.order.ReqOrderCalculateDTO;
import com.example.shinsiri.dtos.req.order.ReqSearchOrderDTO;
import com.example.shinsiri.dtos.req.order.ReqUpdateOrderStatusDTO;
import com.example.shinsiri.dtos.res.order.ResOrderCalculateDTO;
import com.example.shinsiri.dtos.res.order.ResOrderDTO;
import com.example.shinsiri.dtos.res.order.ResSearchOrderDTO;
import com.example.shinsiri.entities.Order;
import com.example.shinsiri.entities.User;
import com.example.shinsiri.repositories.OrderRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private OrderService orderService;


    @GetMapping("")
    public List<Order> getOrders() {
        logger.info("[GET] /orders");
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResOrderDTO getOrderById(
            @PathVariable int id
    ) {
        logger.info("[GET] /orders/{}", id);
        return orderService.getOrderById(id);
    }

    @PostMapping("/search")
    public ResSearchOrderDTO searchOrder(
            Authentication authentication,
            @RequestBody ReqSearchOrderDTO dto
    ) {
        logger.info("[POST] /orders/search with dto: {}", dto);
        User user = authenticationService.checkAuthentication(authentication);
        return orderService.searchOrder(dto, user);
    }

    @PostMapping("/calculate")
    public ResOrderCalculateDTO getOrderCalculate(
            Authentication authentication,
            @Valid @RequestBody ReqOrderCalculateDTO dto
    ) {
        logger.info("[GET] /orders/calculate with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return orderService.getOrderCalculate(dto);
    }

    @PostMapping("")
    public Order createOrder(
            Authentication authentication,
            @Valid @RequestBody ReqCreateOrderDTO dto
    ) {
        logger.info("[POST] /orders with dto: {}", dto);
        User user = authenticationService.checkAuthentication(authentication);
        return orderService.createOrder(dto, user.getId());
    }

    @PutMapping("/{id}")
    public Order updateOrder(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqCreateOrderDTO dto
    ) {
        logger.info("[PUT] /orders/{} with dto: {}", id, dto);
        authenticationService.checkAuthentication(authentication);
        return orderService.updateOrder(id, dto);
    }

    @PutMapping("/status/{id}")
    public Order updateOrderStatus(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqUpdateOrderStatusDTO dto
    ) {
        logger.info("[PUT] /orders/status/{} with dto: {}", id, dto);
        authenticationService.checkAuthentication(authentication);
        return orderService.updateOrderStatus(id, dto);
    }

}

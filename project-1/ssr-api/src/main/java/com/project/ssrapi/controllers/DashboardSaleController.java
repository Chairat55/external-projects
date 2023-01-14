package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.res.dashboard.*;
import com.example.shinsiri.entities.User;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.DashboardSaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboardSale")
public class DashboardSaleController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardSaleController.class);

    @Autowired
    private DashboardSaleService dashboardSaleService;
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/overview")
    public OverviewSaleDTO overview(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate
    ) {
        logger.info("[GET] /dashboardManager/overview - search: {} startDate: {} endDate: {}", search, startDate, endDate);
        User user = authenticationService.checkAuthentication(authentication);
        return dashboardSaleService.getOverview(user.getId(), search, startDate, endDate);
    }

    @GetMapping("/topProducts")
    public ResTopProductsDTO topProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        logger.info("[GET] /dashboardManager/topProducts - search: {} startDate: {} endDate: {} pageNo: {} pageSize: {}",
                search, startDate, endDate, pageNo, pageSize
        );
        User user = authenticationService.checkAuthentication(authentication);
        return dashboardSaleService.topProducts(user.getId(), search, startDate, endDate, pageNo, pageSize);
    }

    @GetMapping("/newCustomers")
    public ResNewCustomersDTO newCustomers(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        logger.info("[GET] /dashboardManager/newCustomers - search: {} startDate: {} endDate: {} pageNo: {} pageSize: {}",
                search, startDate, endDate, pageNo, pageSize
        );
        User user = authenticationService.checkAuthentication(authentication);
        return dashboardSaleService.newCustomers(user.getId(), search, startDate, endDate, pageNo, pageSize);
    }

}

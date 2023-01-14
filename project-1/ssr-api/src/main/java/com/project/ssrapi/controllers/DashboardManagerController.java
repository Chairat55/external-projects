package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.res.dashboard.OverviewManagerDTO;
import com.example.shinsiri.dtos.res.dashboard.ResNewCustomersDTO;
import com.example.shinsiri.dtos.res.dashboard.ResTopProductsDTO;
import com.example.shinsiri.dtos.res.dashboard.ResTopSalesDTO;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.DashboardManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboardManager")
public class DashboardManagerController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardManagerController.class);

    @Autowired
    private DashboardManagerService dashboardManagerService;
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/overview")
    public OverviewManagerDTO overview(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate
    ) {
        logger.info("[GET] /dashboardManager/overview - search: {} startDate: {} endDate: {}", search, startDate, endDate);
        authenticationService.checkAuthentication(authentication);
        return dashboardManagerService.getOverview(search, startDate, endDate);
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
        authenticationService.checkAuthentication(authentication);
        return dashboardManagerService.topProducts(search, startDate, endDate, pageNo, pageSize);
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
        authenticationService.checkAuthentication(authentication);
        return dashboardManagerService.newCustomers(search, startDate, endDate, pageNo, pageSize);
    }

    @GetMapping("/topSales")
    public ResTopSalesDTO topSales(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        logger.info("[GET] /dashboardManager/topSales - search: {} startDate: {} endDate: {} pageNo: {} pageSize: {}",
                search, startDate, endDate, pageNo, pageSize
        );
        authenticationService.checkAuthentication(authentication);
        return dashboardManagerService.topSales(search, startDate, endDate, pageNo, pageSize);
    }

}

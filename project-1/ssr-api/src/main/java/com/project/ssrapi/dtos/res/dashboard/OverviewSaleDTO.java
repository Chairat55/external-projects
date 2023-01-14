package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class OverviewSaleDTO {
    private Double totalSale;
    private Double currentMonthSale;
    private Double lastMonthSale;
    private List<Double> dailySales;

    private int totalOldCustomers;
    private int totalNewCustomers;
    private List<Integer> dailyMeetOldCustomers;
    private List<Integer> dailyMeetNewCustomers;

    private int totalOrders;
    private List<Integer> dailyOrders;

    private CompareLastMonth compareLastMonth = new CompareLastMonth();

    private int countSuccessOrder;
    private int countDraftOrder;
    private int countCancelOrder;

    @Data
    public static class CompareLastMonth {
        private Integer sale;
        private Integer meetCustomer;
        private Integer order;
    }

}

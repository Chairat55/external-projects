package com.project.ssrapi.dtos.res.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class OverviewManagerDTO {
    private Double totalSale;
    private Double currentMonthSale;
    private Double lastMonthSale;
    private List<Double> monthlySales;

    private int totalOldCustomers;
    private int totalNewCustomers;
    private List<Integer> dailyMeetOldCustomers;
    private List<Integer> dailyMeetNewCustomers;

    private int totalActivities;
    private List<Integer> dailyActivities;

    private int totalOrders;
    private List<Integer> dailyOrders;

    private CompareLastMonth compareLastMonth = new CompareLastMonth();
    private ProductTypePercent productTypePercent = new ProductTypePercent();

    private int countSuccessOrder;
    private int countDraftOrder;
    private int countCancelOrder;

    @Data
    public static class CompareLastMonth {
        private Integer sale;
        private Integer meetCustomer;
        private Integer activity;
        private Integer order;
    }

    @Data
    public static class ProductTypePercent {
        private double total;
        private double ssrPercent;
        private double ssrTotal;
        private double lubricantsPercent;
        private double lubricantsTotal;
        private List<ProductTypeDetail> ssrDetails;
        private List<ProductTypeDetail> lubricantsDetails;

        @Data
        public static class ProductTypeDetail {
            private String name;
            private double percent;
            private double total;
        }
    }

}

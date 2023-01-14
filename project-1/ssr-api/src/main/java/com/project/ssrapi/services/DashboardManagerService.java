package com.project.ssrapi.services;

import com.example.shinsiri.dtos.res.dashboard.*;
import com.example.shinsiri.entities.Order;
import com.example.shinsiri.entities.UserImage;
import com.example.shinsiri.repositories.OrderRepository;
import com.example.shinsiri.repositories.UserImageRepository;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardManagerService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardManagerService.class);

    @Autowired
    private UserImageRepository userImageRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public OverviewManagerDTO getOverview(String search, String startDate, String endDate) {
        OverviewManagerDTO overviewManagerDTO = new OverviewManagerDTO();

        Calendar now = Calendar.getInstance();
        String currentDay = String.format("%02d", now.get(Calendar.DAY_OF_MONTH));
        String currentMonth = String.format("%02d", now.get(Calendar.MONTH) + 1);
        String currentYear = String.valueOf(now.get(Calendar.YEAR));

        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            getMonthlySales(currentMonth, currentYear, overviewManagerDTO);
            getMeetCustomers(currentDay, firstDayOfThisMonth, overviewManagerDTO);
            getActivities(currentDay, firstDayOfThisMonth, overviewManagerDTO);
            getOrders(currentDay, firstDayOfThisMonth, overviewManagerDTO);
            getProductTypePercent(firstDayOfThisMonth, overviewManagerDTO);
            getCountOrderByStatus(firstDayOfThisMonth, overviewManagerDTO);
        } else {
            getMonthlySalesBySearch(search, startDate, endDate, overviewManagerDTO);
            getMeetCustomersBySearch(search, startDate, endDate, overviewManagerDTO);
            getActivitiesBySearch(search, startDate, endDate, overviewManagerDTO);
            getOrdersBySearch(search, startDate, endDate, overviewManagerDTO);
            getProductTypePercentBySearch(search, startDate, endDate, overviewManagerDTO);
            getCountOrderByStatusBySearch(search, startDate, endDate, overviewManagerDTO);
        }

        return overviewManagerDTO;
    }

    public ResTopProductsDTO topProducts(String search, String startDate, String endDate, int pageNo, int pageSize) {
        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            return getTopProducts(firstDayOfThisMonth, pageNo, pageSize);
        } else {
            return getTopProductsBySearch(search, startDate, endDate, pageNo, pageSize);
        }
    }

    public ResNewCustomersDTO newCustomers(String search, String startDate, String endDate, int pageNo, int pageSize) {
        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            return getNewCustomers(firstDayOfThisMonth, pageNo, pageSize);
        } else {
            return getNewCustomersBySearch(search, startDate, endDate, pageNo, pageSize);
        }
    }

    public ResTopSalesDTO topSales(String search, String startDate, String endDate, int pageNo, int pageSize) {
        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            return getTopSales(firstDayOfThisMonth, pageNo, pageSize);
        } else {
            return getTopSalesBySearch(search, startDate, endDate, pageNo, pageSize);
        }
    }

    private void getMonthlySales(String currentMonth, String currentYear, OverviewManagerDTO overviewManagerDTO) {
        try {
            // เดือน 1 ไม่ต้อง compare lastMonth
            String lastMonth = Integer.parseInt(currentMonth) == 1 ? "NOT_COMPARE" : String.format("%02d", Integer.parseInt(currentMonth) - 1);

            String sqlMonthlySales = "SELECT TO_CHAR(created_date , 'MM') AS month, SUM(net) AS total " +
                    "FROM orders " +
                    "WHERE TO_CHAR(created_date , 'YYYY') = :currentYear " +
                    "GROUP BY TO_CHAR(created_date , 'MM')" +
                    "ORDER BY to_char(created_date , 'MM') ";

            Query query = em.createNativeQuery(sqlMonthlySales).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlMonthlyDTO.class));

            query.setParameter("currentYear", currentYear);

            List<Double> monthlySales = new ArrayList<>();
            List<ResSqlMonthlyDTO> monthlySaleResults = query.getResultList();

            // monthlySales
            for (int i = 1; i <= 12; i++) {
                String month = String.format("%02d", i);

                ResSqlMonthlyDTO findByMonth = monthlySaleResults.stream()
                        .filter(monthlySaleResult -> monthlySaleResult.getMonth().equals(month))
                        .findFirst()
                        .orElse(null);

                Double totalThisMonth = findByMonth == null ? (i > Integer.parseInt(currentMonth) ? null : 0.0) : Double.valueOf(findByMonth.getTotal().doubleValue());
                monthlySales.add(totalThisMonth);

                // currentMonthSale
                if (month.equals(lastMonth)) {
                    overviewManagerDTO.setLastMonthSale(totalThisMonth);
                } else if (month.equals(currentMonth)) {
                    overviewManagerDTO.setCurrentMonthSale(totalThisMonth);
                }
            }

            overviewManagerDTO.setMonthlySales(monthlySales);

            // CompareLastMonth sale
            int compareSaleLastMonth;
            if (overviewManagerDTO.getLastMonthSale() != null && overviewManagerDTO.getLastMonthSale() != 0.0) {

                if (overviewManagerDTO.getLastMonthSale() > overviewManagerDTO.getCurrentMonthSale()) {
                    compareSaleLastMonth = (int) (
                            overviewManagerDTO.getCurrentMonthSale()
                                    / overviewManagerDTO.getLastMonthSale() * 100 - 100
                    );

                } else {
                    compareSaleLastMonth = (int) (
                            (overviewManagerDTO.getCurrentMonthSale() - overviewManagerDTO.getLastMonthSale())
                                    / overviewManagerDTO.getLastMonthSale() * 100
                    );
                }

                OverviewManagerDTO.CompareLastMonth compareLastMonth = overviewManagerDTO.getCompareLastMonth();
                compareLastMonth.setSale(compareSaleLastMonth);

                overviewManagerDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getMonthlySales: {}", e.getMessage());
        }
    }

    private void getMonthlySalesBySearch(String search, String startDate, String endDate, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlMonthlySalesBySearch = "SELECT SUM(o.net) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE 1=1 ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlMonthlySalesBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlMonthlySalesBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlMonthlySalesBySearch);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            double totalSale = ((BigDecimal) query.getSingleResult()).doubleValue();

            overviewManagerDTO.setTotalSale(totalSale);

        } catch (Exception e) {
            logger.info("Can't getMonthlySalesBySearch: {}", e.getMessage());
        }
    }

    private void getMeetCustomers(String currentDay, Calendar firstDayOfThisMonth, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlDailyMeetNewCustomers = "SELECT TO_CHAR(created_date , 'DD') as day, COUNT(id) as count " +
                    "FROM customers " +
                    "WHERE created_date >= :firstDayOfThisMonth " +
                    "GROUP BY TO_CHAR(created_date , 'DD') " +
                    "ORDER BY TO_CHAR(created_date , 'DD') ";

            String sqlDailyMeetOldCustomers = "SELECT TO_CHAR(created_date , 'DD') as day, COUNT(id) as count " +
                    "FROM customers " +
                    "WHERE created_date < :firstDayOfThisMonth " +
                    "GROUP BY TO_CHAR(created_date , 'DD') " +
                    "ORDER BY TO_CHAR(created_date , 'DD') ";

            Query queryNew = em.createNativeQuery(sqlDailyMeetNewCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            Query queryOld = em.createNativeQuery(sqlDailyMeetOldCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            queryNew.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryOld.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            List<Integer> dailyMeetNewCustomers = new ArrayList<>();
            List<Integer> dailyMeetOldCustomers = new ArrayList<>();
            List<ResSqlDailyDTO> dailyMeetNewCustomerResults = queryNew.getResultList();
            List<ResSqlDailyDTO> dailyMeetOldCustomerResults = queryOld.getResultList();

            // totalOldCustomers, totalNewCustomers
            overviewManagerDTO.setTotalNewCustomers(dailyMeetNewCustomerResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());
            overviewManagerDTO.setTotalOldCustomers(dailyMeetOldCustomerResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());

            // dailyMeetNewCustomers, dailyMeetOldCustomers
            for (int i = 1; i <= 31; i++) {
                String day = String.format("%02d", i);

                // dailyMeetNewCustomers
                ResSqlDailyDTO findNewByDay = dailyMeetNewCustomerResults.stream()
                        .filter(dailyMeetNewCustomerResult -> dailyMeetNewCustomerResult.getDay().equals(day))
                        .findFirst()
                        .orElse(null);

                // ถ้าวันที่ มากกว่า วันที่ของเดืิอนปัจจุบัน จะเป็น null แปลว่ายังหา ยอดรวมไม่ได้
                Integer totalNewThisDay = findNewByDay == null ? (i > Integer.parseInt(currentDay) ? null : 0) : Integer.valueOf(findNewByDay.getCount().intValue());
                dailyMeetNewCustomers.add(totalNewThisDay);

                // dailyMeetOldCustomers
                ResSqlDailyDTO findOldByDay = dailyMeetOldCustomerResults.stream()
                        .filter(dailyMeetNewCustomerResult -> dailyMeetNewCustomerResult.getDay().equals(day))
                        .findFirst()
                        .orElse(null);

                int totalOldThisDay = findOldByDay == null ? 0 : findOldByDay.getCount().intValue();
                dailyMeetOldCustomers.add(totalOldThisDay);
            }

            overviewManagerDTO.setDailyMeetNewCustomers(dailyMeetNewCustomers);
            overviewManagerDTO.setDailyMeetOldCustomers(dailyMeetOldCustomers);

//          // CompareLastMonth meetCustomer
            int compareMeetCustomerLastMonth;
            if (overviewManagerDTO.getTotalOldCustomers() != 0) {

                if (overviewManagerDTO.getTotalOldCustomers() > overviewManagerDTO.getTotalNewCustomers()) {
                    compareMeetCustomerLastMonth = overviewManagerDTO.getTotalNewCustomers()
                            / overviewManagerDTO.getTotalOldCustomers() * 100 - 100;

                } else {
                    compareMeetCustomerLastMonth = (overviewManagerDTO.getTotalNewCustomers() - overviewManagerDTO.getTotalOldCustomers())
                            / overviewManagerDTO.getTotalOldCustomers() * 100;
                }

                OverviewManagerDTO.CompareLastMonth compareLastMonth = overviewManagerDTO.getCompareLastMonth();
                compareLastMonth.setMeetCustomer(compareMeetCustomerLastMonth);

                overviewManagerDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getMeetCustomers: {}", e.getMessage());
        }
    }

    private void getMeetCustomersBySearch(String search, String startDate, String endDate, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlNewCustomersBySearch = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "WHERE 1=1 ";

            String sqlAllCustomer = "SELECT COUNT(id) " +
                    "FROM customers ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlNewCustomersBySearch += "    AND TO_CHAR(c.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlNewCustomersBySearch += "    OR c.full_name LIKE :search ";
            }

            Query queryBySearch = em.createNativeQuery(sqlNewCustomersBySearch);
            Query queryAll = em.createNativeQuery(sqlAllCustomer);

            if (!startDate.equals("") && !endDate.equals("")) {
                queryBySearch.setParameter("startDate", startDate);
                queryBySearch.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                queryBySearch.setParameter("search", "%" + search + "%");
            }

            int totalBySearch = ((BigInteger) queryBySearch.getSingleResult()).intValue();
            int totalAll = ((BigInteger) queryAll.getSingleResult()).intValue();

            // totalOldCustomers, totalNewCustomers
            overviewManagerDTO.setTotalNewCustomers(totalBySearch);
            overviewManagerDTO.setTotalOldCustomers(totalAll - totalBySearch);

        } catch (Exception e) {
            logger.info("Can't getMeetCustomersBySearch: {}", e.getMessage());
        }
    }

    private void getActivities(String currentDay, Calendar firstDayOfThisMonth, OverviewManagerDTO overviewManagerDTO) {
        try {
            Calendar firstDayOfLastMonth = Calendar.getInstance();
            firstDayOfLastMonth.add(Calendar.MONTH, -1);
            firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

            // Query daily current month
            String sqlDailyActivities = "SELECT TO_CHAR(created_date , 'DD') as day, COUNT(id) as count " +
                    "FROM orders " +
                    "WHERE created_date >= :firstDayOfThisMonth" +
                    "   AND status IN ('PREPARE', 'SHIPPING', 'SHIPPED') " +
                    "GROUP BY TO_CHAR(created_date , 'DD') " +
                    "ORDER BY TO_CHAR(created_date , 'DD') ";

            Query query = em.createNativeQuery(sqlDailyActivities).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            // Query last month เพื่อเอามา compare
            String sqlLastMonthActivities = "SELECT TO_CHAR(created_date , 'DD') as day, COUNT(id) as count " +
                    "FROM orders " +
                    "WHERE created_date >= :firstDayOfLastMonth " +
                    "   AND created_date < :firstDayOfThisMonth " +
                    "   AND status IN ('PREPARE', 'SHIPPING', 'SHIPPED') " +
                    "GROUP BY TO_CHAR(created_date , 'DD') " +
                    "ORDER BY TO_CHAR(created_date , 'DD') ";

            Query queryLastMonth = em.createNativeQuery(sqlLastMonthActivities).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            queryLastMonth.setParameter("firstDayOfLastMonth", firstDayOfLastMonth);
            queryLastMonth.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            List<Integer> dailyActivities = new ArrayList<>();
            List<ResSqlDailyDTO> dailyActivityResults = query.getResultList();
            List<ResSqlDailyDTO> dailyLastMonthActivityResults = queryLastMonth.getResultList();

            // totalActivities
            overviewManagerDTO.setTotalActivities(dailyActivityResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());

            // dailyOrders
            for (int i = 1; i <= 31; i++) {
                String day = String.format("%02d", i);

                ResSqlDailyDTO findByDay = dailyActivityResults.stream()
                        .filter(dailyActivityResult -> dailyActivityResult.getDay().equals(day))
                        .findFirst()
                        .orElse(null);

                // ถ้าวันที่ มากกว่า วันที่ของเดืิอนปัจจุบัน จะเป็น null แปลว่ายังหา ยอดรวมไม่ได้
                Integer totalThisDay = findByDay == null ? (i > Integer.parseInt(currentDay) ? null : 0) : Integer.valueOf(findByDay.getCount().intValue());
                dailyActivities.add(totalThisDay);
            }

            overviewManagerDTO.setDailyActivities(dailyActivities);

            // CompareLastMonth activity
            int compareActivityLastMonth;
            if (dailyLastMonthActivityResults.size() > 0) {

                int countLastMonthActivity = dailyLastMonthActivityResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum();

                if (countLastMonthActivity > overviewManagerDTO.getTotalOrders()) {
                    compareActivityLastMonth = overviewManagerDTO.getTotalOrders()
                            / countLastMonthActivity * 100 - 100;

                } else {
                    compareActivityLastMonth = (overviewManagerDTO.getTotalOrders() - countLastMonthActivity)
                            / countLastMonthActivity * 100;
                }

                OverviewManagerDTO.CompareLastMonth compareLastMonth = overviewManagerDTO.getCompareLastMonth();
                compareLastMonth.setActivity(compareActivityLastMonth);

                overviewManagerDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getActivities: {}", e.getMessage());
        }
    }

    private void getActivitiesBySearch(String search, String startDate, String endDate, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlActivitiesBySearch = "SELECT COUNT(o.id) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.status IN ('PREPARE', 'SHIPPING', 'SHIPPED') ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlActivitiesBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlActivitiesBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlActivitiesBySearch);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            // totalActivities
            overviewManagerDTO.setTotalActivities(((BigInteger) query.getSingleResult()).intValue());

        } catch (Exception e) {
            logger.info("Can't getActivitiesBySearch: {}", e.getMessage());
        }
    }

    private void getOrders(String currentDay, Calendar firstDayOfThisMonth, OverviewManagerDTO overviewManagerDTO) {
        try {
            Calendar firstDayOfLastMonth = Calendar.getInstance();
            firstDayOfLastMonth.add(Calendar.MONTH, -1);
            firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

            // Query daily current month
            String sqlDailyOrders = "SELECT TO_CHAR(created_date , 'DD') as day, COUNT(id) as count " +
                    "FROM orders " +
                    "WHERE created_date >= :firstDayOfThisMonth " +
                    "GROUP BY TO_CHAR(created_date , 'DD') " +
                    "ORDER BY TO_CHAR(created_date , 'DD') ";

            Query query = em.createNativeQuery(sqlDailyOrders).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            // Query last month เพื่อเอามา compare
            String sqlLastMonthOrders = "SELECT TO_CHAR(created_date , 'DD') as day, COUNT(id) as count " +
                    "FROM orders " +
                    "WHERE created_date >= :firstDayOfLastMonth " +
                    "   AND created_date < :firstDayOfThisMonth " +
                    "GROUP BY TO_CHAR(created_date , 'DD') " +
                    "ORDER BY TO_CHAR(created_date , 'DD') ";

            Query queryLastMonth = em.createNativeQuery(sqlLastMonthOrders).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            queryLastMonth.setParameter("firstDayOfLastMonth", firstDayOfLastMonth);
            queryLastMonth.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            List<Integer> dailyOrders = new ArrayList<>();
            List<ResSqlDailyDTO> dailyOrderResults = query.getResultList();
            List<ResSqlDailyDTO> dailyLastMonthOrderResults = queryLastMonth.getResultList();

            // totalOrders
            overviewManagerDTO.setTotalOrders(dailyOrderResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());

            // dailyOrders
            for (int i = 1; i <= 31; i++) {
                String day = String.format("%02d", i);

                ResSqlDailyDTO findByDay = dailyOrderResults.stream()
                        .filter(dailyOrderResult -> dailyOrderResult.getDay().equals(day))
                        .findFirst()
                        .orElse(null);

                // ถ้าวันที่ มากกว่า วันที่ของเดืิอนปัจจุบัน จะเป็น null แปลว่ายังหา ยอดรวมไม่ได้
                Integer totalThisDay = findByDay == null ? (i > Integer.parseInt(currentDay) ? null : 0) : Integer.valueOf(findByDay.getCount().intValue());
                dailyOrders.add(totalThisDay);
            }

            overviewManagerDTO.setDailyOrders(dailyOrders);

            // CompareLastMonth order
            int compareOrderLastMonth;
            if (dailyLastMonthOrderResults.size() > 0) {

                int countLastMonthOrder = dailyLastMonthOrderResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum();

                if (countLastMonthOrder > overviewManagerDTO.getTotalOrders()) {
                    compareOrderLastMonth = overviewManagerDTO.getTotalOrders()
                            / countLastMonthOrder * 100 - 100;

                } else {
                    compareOrderLastMonth = (overviewManagerDTO.getTotalOrders() - countLastMonthOrder)
                            / countLastMonthOrder * 100;
                }

                OverviewManagerDTO.CompareLastMonth compareLastMonth = overviewManagerDTO.getCompareLastMonth();
                compareLastMonth.setOrder(compareOrderLastMonth);

                overviewManagerDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getOrders: {}", e.getMessage());
        }
    }

    private void getOrdersBySearch(String search, String startDate, String endDate, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlOrdersBySearch = "SELECT COUNT(o.id) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE 1=1 ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlOrdersBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlOrdersBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlOrdersBySearch);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            // totalOrders
            overviewManagerDTO.setTotalOrders(((BigInteger) query.getSingleResult()).intValue());

        } catch (Exception e) {
            logger.info("Can't getOrdersBySearch: {}", e.getMessage());
        }
    }

    private void getProductTypePercent(Calendar firstDayOfThisMonth, OverviewManagerDTO overviewManagerDTO) {
        try {
            OverviewManagerDTO.ProductTypePercent productTypePercent = new OverviewManagerDTO.ProductTypePercent();

            String sqlSumProductType = "SELECT p.type as producttype, SUM(o.net) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "WHERE o.created_date >= :firstDayOfThisMonth " +
                    "GROUP BY p.type ";

            Query query = em.createNativeQuery(sqlSumProductType).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlSumProductTypeDTO.class));

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            List<ResSqlSumProductTypeDTO> sumProductTypeResults = query.getResultList();

            // Loop product type เพื่อ set total
            List<OverviewManagerDTO.ProductTypePercent.ProductTypeDetail> ssrDetails = new ArrayList<>();
            List<OverviewManagerDTO.ProductTypePercent.ProductTypeDetail> lubricantsDetails = new ArrayList<>();

            for (int i=0 ; i<ProductService.PRODUCT_TYPES.size() ; i++) {
                String productType = ProductService.PRODUCT_TYPES.get(i);

                OverviewManagerDTO.ProductTypePercent.ProductTypeDetail productTypeDetail = new OverviewManagerDTO.ProductTypePercent.ProductTypeDetail();
                productTypeDetail.setName(productType);

                Optional<ResSqlSumProductTypeDTO> findResult = sumProductTypeResults.stream()
                        .filter(result -> result.getProducttype().equals(productType))
                        .findFirst();

                findResult.ifPresent(resSqlSumProductTypeDTO -> productTypeDetail.setTotal(resSqlSumProductTypeDTO.getSum().doubleValue()));

                if (ProductService.SSR_TYPES.contains(productType)) {
                    ssrDetails.add(productTypeDetail);
                } else if (ProductService.LUBRICANTS_TYPES.contains(productType)) {
                    lubricantsDetails.add(productTypeDetail);
                }
            }

            productTypePercent.setSsrDetails(ssrDetails);
            productTypePercent.setLubricantsDetails(lubricantsDetails);

            // ทำ %
            double total = sumProductTypeResults.stream().map(r -> r.getSum().doubleValue()).mapToDouble(Double::doubleValue).sum();
            double totalSsr = ssrDetails.stream().map(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getTotal).mapToDouble(Double::doubleValue).sum();
            double totalLubricants = lubricantsDetails.stream().map(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getTotal).mapToDouble(Double::doubleValue).sum();

            productTypePercent.setTotal(total);

            if (total != 0 && (totalSsr != 0 || totalLubricants != 0)) {
                if (totalSsr != 0) {
                    productTypePercent.setSsrPercent(Math.round((totalSsr / total * 100) * 100.0) / 100.0);
                    productTypePercent.setLubricantsPercent(100 - productTypePercent.getSsrPercent());
                } else {
                    productTypePercent.setLubricantsPercent(Math.round((totalLubricants / total * 100) * 100.0) / 100.0);
                    productTypePercent.setSsrPercent(100 - productTypePercent.getLubricantsPercent());
                }

                productTypePercent.setSsrTotal(totalSsr);
                productTypePercent.setLubricantsTotal(totalLubricants);
            }

            if (totalSsr > 0) {
                // ทำเป็น %
                ssrDetails.forEach(ssrDetail -> {
                    ssrDetail.setPercent(Math.round((ssrDetail.getTotal() / totalLubricants * 100) * 100.0) / 100.0);
                });

                // Sort ตาม %
                productTypePercent.setSsrDetails(
                        ssrDetails.stream()
                                .sorted(Comparator.comparingDouble(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getPercent).reversed())
                                .collect(Collectors.toList())
                );
            }

            if (totalLubricants > 0) {
                // ทำเป็น %
                lubricantsDetails.forEach(lubricantsDetail -> {
                    lubricantsDetail.setPercent(Math.round((lubricantsDetail.getTotal() / totalLubricants * 100) * 100.0) / 100.0);
                });

                // Sort ตาม %
                productTypePercent.setLubricantsDetails(
                        lubricantsDetails.stream()
                                .sorted(Comparator.comparingDouble(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getPercent).reversed())
                                .collect(Collectors.toList())
                );
            }

            overviewManagerDTO.setProductTypePercent(productTypePercent);

        } catch (Exception e) {
            logger.info("Can't getProductTypePercent: {}", e.getMessage());
        }
    }

    private void getProductTypePercentBySearch(String search, String startDate, String endDate, OverviewManagerDTO overviewManagerDTO) {
        try {
            OverviewManagerDTO.ProductTypePercent productTypePercent = new OverviewManagerDTO.ProductTypePercent();

            String sqlSumProductTypeBySearch = "SELECT p.type as producttype, SUM(o.net) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE 1=1 ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlSumProductTypeBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlSumProductTypeBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            sqlSumProductTypeBySearch += "  GROUP BY p.type ";

            Query query = em.createNativeQuery(sqlSumProductTypeBySearch).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlSumProductTypeDTO.class));

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            List<ResSqlSumProductTypeDTO> sumProductTypeResults = query.getResultList();

            // Loop product type เพื่อ set total
            List<OverviewManagerDTO.ProductTypePercent.ProductTypeDetail> ssrDetails = new ArrayList<>();
            List<OverviewManagerDTO.ProductTypePercent.ProductTypeDetail> lubricantsDetails = new ArrayList<>();

            for (int i=0 ; i<ProductService.PRODUCT_TYPES.size() ; i++) {
                String productType = ProductService.PRODUCT_TYPES.get(i);

                OverviewManagerDTO.ProductTypePercent.ProductTypeDetail productTypeDetail = new OverviewManagerDTO.ProductTypePercent.ProductTypeDetail();
                productTypeDetail.setName(productType);

                Optional<ResSqlSumProductTypeDTO> findResult = sumProductTypeResults.stream()
                        .filter(result -> result.getProducttype().equals(productType))
                        .findFirst();

                findResult.ifPresent(resSqlSumProductTypeDTO -> productTypeDetail.setTotal(resSqlSumProductTypeDTO.getSum().doubleValue()));

                if (ProductService.SSR_TYPES.contains(productType)) {
                    ssrDetails.add(productTypeDetail);
                } else if (ProductService.LUBRICANTS_TYPES.contains(productType)) {
                    lubricantsDetails.add(productTypeDetail);
                }
            }

            productTypePercent.setSsrDetails(ssrDetails);
            productTypePercent.setLubricantsDetails(lubricantsDetails);

            // ทำ %
            double total = sumProductTypeResults.stream().map(r -> r.getSum().doubleValue()).mapToDouble(Double::doubleValue).sum();
            double totalSsr = ssrDetails.stream().map(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getTotal).mapToDouble(Double::doubleValue).sum();
            double totalLubricants = lubricantsDetails.stream().map(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getTotal).mapToDouble(Double::doubleValue).sum();

            productTypePercent.setTotal(total);

            if (total != 0 && (totalSsr != 0 || totalLubricants != 0)) {
                if (totalSsr != 0) {
                    productTypePercent.setSsrPercent(Math.round((totalSsr / total * 100) * 100.0) / 100.0);
                    productTypePercent.setLubricantsPercent(100 - productTypePercent.getSsrPercent());
                } else {
                    productTypePercent.setLubricantsPercent(Math.round((totalLubricants / total * 100) * 100.0) / 100.0);
                    productTypePercent.setSsrPercent(100 - productTypePercent.getLubricantsPercent());
                }

                productTypePercent.setSsrTotal(totalSsr);
                productTypePercent.setLubricantsTotal(totalLubricants);
            }

            if (totalSsr > 0) {
                // ทำเป็น %
                ssrDetails.forEach(ssrDetail -> {
                    ssrDetail.setPercent(Math.round((ssrDetail.getTotal() / totalLubricants * 100) * 100.0) / 100.0);
                });

                // Sort ตาม %
                productTypePercent.setSsrDetails(
                        ssrDetails.stream()
                                .sorted(Comparator.comparingDouble(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getPercent).reversed())
                                .collect(Collectors.toList())
                );
            }

            if (totalLubricants > 0) {
                // ทำเป็น %
                lubricantsDetails.forEach(lubricantsDetail -> {
                    lubricantsDetail.setPercent(Math.round((lubricantsDetail.getTotal() / totalLubricants * 100) * 100.0) / 100.0);
                });

                // Sort ตาม %
                productTypePercent.setLubricantsDetails(
                        lubricantsDetails.stream()
                                .sorted(Comparator.comparingDouble(OverviewManagerDTO.ProductTypePercent.ProductTypeDetail::getPercent).reversed())
                                .collect(Collectors.toList())
                );
            }

            overviewManagerDTO.setProductTypePercent(productTypePercent);

        } catch (Exception e) {
            logger.info("Can't getProductTypePercentBySearch: {}", e.getMessage());
        }
    }

    private void getCountOrderByStatus(Calendar firstDayOfThisMonth, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlCountOrderByStatus = "SELECT o.* " +
                    "FROM orders o " +
                    "WHERE o.created_date >= :firstDayOfThisMonth " +
                    "   AND o.status IN ('WAIT_LICENSE_1', 'WAIT_LICENSE_2', 'DELETE', 'SHIPPED') ";

            Query query = em.createNativeQuery(sqlCountOrderByStatus, Order.class);
            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            List<Order> results = query.getResultList();

            if (results.size() > 0) {
                overviewManagerDTO.setCountSuccessOrder((int) results.stream().filter(r -> r.getStatus().equals("SHIPPED")).count());
                overviewManagerDTO.setCountDraftOrder((int) results.stream().filter(r -> r.getStatus().equals("WAIT_LICENSE_1") || r.getStatus().equals("WAIT_LICENSE_2")).count());
                overviewManagerDTO.setCountCancelOrder((int) results.stream().filter(r -> r.getStatus().equals("DELETE")).count());
            }

        } catch (Exception e) {
            logger.info("Can't getCountOrderByStatus: {}", e.getMessage());
        }
    }

    private void getCountOrderByStatusBySearch(String search, String startDate, String endDate, OverviewManagerDTO overviewManagerDTO) {
        try {
            String sqlCountOrderByStatusBySearch = "SELECT o.* " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE 1=1 " +
                    "   AND o.status IN ('WAIT_LICENSE_1', 'WAIT_LICENSE_2', 'DELETE', 'SHIPPED') ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlCountOrderByStatusBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlCountOrderByStatusBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlCountOrderByStatusBySearch, Order.class);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            List<Order> results = query.getResultList();

            if (results.size() > 0) {
                overviewManagerDTO.setCountSuccessOrder((int) results.stream().filter(r -> r.getStatus().equals("SHIPPED")).count());
                overviewManagerDTO.setCountDraftOrder((int) results.stream().filter(r -> r.getStatus().equals("WAIT_LICENSE_1") || r.getStatus().equals("WAIT_LICENSE_2")).count());
                overviewManagerDTO.setCountCancelOrder((int) results.stream().filter(r -> r.getStatus().equals("DELETE")).count());
            }

        } catch (Exception e) {
            logger.info("Can't getCountOrderByStatusBySearch: {}", e.getMessage());
        }
    }

    private ResTopProductsDTO getTopProducts(Calendar firstDayOfThisMonth, int pageNo, int pageSize) {
        ResTopProductsDTO resDto = new ResTopProductsDTO();

        try {
            String sqlTopProducts = "SELECT p.name, p.type, SUM(o.net) as total " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "WHERE o.created_date >= :firstDayOfThisMonth " +
                    "GROUP BY p.name, p.type " +
                    "ORDER BY SUM(o.net) DESC";

            String sqlCountTopProducts = "SELECT COUNT(*) " +
                    "FROM (" +
                    "   SELECT COUNT(p.name) " +
                    "   FROM orders o " +
                    "   JOIN order_details od ON o.id = od.order_id " +
                    "   JOIN products p ON od.product_id = p.id " +
                    "   WHERE o.created_date >= :firstDayOfThisMonth " +
                    "   GROUP BY p.name, p.type " +
                    ") as count ";

            Query query = em.createNativeQuery(sqlTopProducts).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlTopProductsDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountTopProducts);

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryCount.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            query.setFirstResult((pageNo - 1) * pageSize);
            query.setMaxResults(pageSize);

            int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();

            resDto.setPageNo(pageNo);
            resDto.setPageSize(pageSize);
            resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
            resDto.setTotalItems(totalItems);
            resDto.setItems(query.getResultList());

        } catch (Exception e) {
            logger.info("Can't getTopProduct: {}", e.getMessage());
        }

        return resDto;
    }

    private ResTopProductsDTO getTopProductsBySearch(String search, String startDate, String endDate, int pageNo, int pageSize) {
        ResTopProductsDTO resDto = new ResTopProductsDTO();

        try {
            String sqlTopProducts = "SELECT p.name, p.type, SUM(o.net) as total " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE 1=1 ";

            String sqlCountTopProducts = "SELECT COUNT(*) " +
                    "FROM (" +
                    "   SELECT COUNT(p.name) " +
                    "   FROM orders o " +
                    "   JOIN order_details od ON o.id = od.order_id " +
                    "   JOIN products p ON od.product_id = p.id " +
                    "   JOIN customers c ON o.customer_id = c.id " +
                    "   JOIN users u ON o.user_id = u.id " +
                    "   WHERE 1=1 ";


            if (!startDate.equals("") && !endDate.equals("")) {
                sqlTopProducts += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
                sqlCountTopProducts += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlTopProducts += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
                sqlCountTopProducts += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            sqlTopProducts += "GROUP BY p.name, p.type ORDER BY SUM(o.net) DESC ";
            sqlCountTopProducts += "   GROUP BY p.name, p.type) as count ";

            Query query = em.createNativeQuery(sqlTopProducts).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlTopProductsDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountTopProducts);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
                queryCount.setParameter("startDate", startDate);
                queryCount.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
                queryCount.setParameter("search", "%" + search + "%");
            }

            query.setFirstResult((pageNo - 1) * pageSize);
            query.setMaxResults(pageSize);

            int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();

            resDto.setPageNo(pageNo);
            resDto.setPageSize(pageSize);
            resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
            resDto.setTotalItems(totalItems);
            resDto.setItems(query.getResultList());

        } catch (Exception e) {
            logger.info("Can't getTopProductsBySearch: {}", e.getMessage());
        }

        return resDto;
    }

    private ResNewCustomersDTO getNewCustomers(Calendar firstDayOfThisMonth, int pageNo, int pageSize) {
        ResNewCustomersDTO resDto = new ResNewCustomersDTO();

        try {
            String sqlNewCustomers = "SELECT c.full_name as fullName " +
                    "FROM customers c " +
                    "WHERE c.created_date >= :firstDayOfThisMonth " +
                    "ORDER BY c.created_date DESC ";

            String sqlCountNewCustomers = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "WHERE c.created_date >= :firstDayOfThisMonth ";

            Query query = em.createNativeQuery(sqlNewCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlNewCustomersDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountNewCustomers);

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryCount.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            query.setFirstResult((pageNo - 1) * pageSize);
            query.setMaxResults(pageSize);

            int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();

            resDto.setPageNo(pageNo);
            resDto.setPageSize(pageSize);
            resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
            resDto.setTotalItems(totalItems);
            resDto.setItems(query.getResultList());

        } catch (Exception e) {
            logger.info("Can't getNewCustomers: {}", e.getMessage());
        }

        return resDto;
    }

    private ResNewCustomersDTO getNewCustomersBySearch(String search, String startDate, String endDate, int pageNo, int pageSize) {
        ResNewCustomersDTO resDto = new ResNewCustomersDTO();

        try {
            String sqlNewCustomers = "SELECT c.full_name as fullName " +
                    "FROM customers c " +
                    "WHERE 1=1 ";

            String sqlCountNewCustomers = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "WHERE 1=1 ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlNewCustomers += "    AND TO_CHAR(c.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
                sqlCountNewCustomers += "    AND TO_CHAR(c.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlNewCustomers += "    OR c.full_name LIKE :search ";
                sqlCountNewCustomers += "    OR c.full_name LIKE :search ";
            }

            sqlNewCustomers += "ORDER BY c.created_date DESC ";


            Query query = em.createNativeQuery(sqlNewCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlNewCustomersDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountNewCustomers);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
                queryCount.setParameter("startDate", startDate);
                queryCount.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
                queryCount.setParameter("search", "%" + search + "%");
            }

            query.setFirstResult((pageNo - 1) * pageSize);
            query.setMaxResults(pageSize);

            int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();

            resDto.setPageNo(pageNo);
            resDto.setPageSize(pageSize);
            resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
            resDto.setTotalItems(totalItems);
            resDto.setItems(query.getResultList());

        } catch (Exception e) {
            logger.info("Can't getNewCustomersBySearch: {}", e.getMessage());
        }

        return resDto;
    }

    private ResTopSalesDTO getTopSales(Calendar firstDayOfThisMonth, int pageNo, int pageSize) {
        ResTopSalesDTO resDto = new ResTopSalesDTO();

        try {
            String sqlTopSales = "SELECT u.id as userId, " +
                    "   u.full_name as fullName, " +
                    "   u.tel, " +
                    "   SUM(o.total) as total, " +
                    "   COUNT(o.id) as countOrder " +
                    "FROM orders o " +
                    "JOIN users u on o.user_id = u.id " +
                    "WHERE o.created_date >= :firstDayOfThisMonth " +
                    "GROUP BY u.id, u.full_name, u.tel " +
                    "ORDER BY SUM(o.total) DESC ";

            String sqlCountTopSales = "SELECT COUNT(a.count), SUM(a.sum) " +
                    "FROM ( " +
                    "   SELECT COUNT(u.id), SUM(o.total) " +
                    "   FROM orders o " +
                    "   JOIN users u on o.user_id = u.id " +
                    "   WHERE o.created_date >= :firstDayOfThisMonth " +
                    "   GROUP BY u.id " +
                    ") as a ";

            Query query = em.createNativeQuery(sqlTopSales).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlTopSalesDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountTopSales).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlCountTopSalesDTO.class));

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryCount.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);

            query.setFirstResult((pageNo - 1) * pageSize);
            query.setMaxResults(pageSize);

            ResSqlCountTopSalesDTO resultCount = (ResSqlCountTopSalesDTO) queryCount.getSingleResult();

            List<ResSqlTopSalesDTO> results = mapPercentAndImageToResultTopSales(query.getResultList(), resultCount.getSum().doubleValue());

            resDto.setPageNo(pageNo);
            resDto.setPageSize(pageSize);
            resDto.setTotalPages((int) Math.ceil(resultCount.getCount().intValue() / (pageSize + 0.0)));
            resDto.setTotalItems(resultCount.getCount().intValue());
            resDto.setItems(results);

        } catch (Exception e) {
            logger.info("Can't getTopSales: {}", e.getMessage());
        }

        return resDto;
    }

    private ResTopSalesDTO getTopSalesBySearch(String search, String startDate, String endDate, int pageNo, int pageSize) {
        ResTopSalesDTO resDto = new ResTopSalesDTO();

        try {
            String sqlTopSales = "SELECT u.id as userId, " +
                    "   u.full_name as fullName, " +
                    "   u.tel, " +
                    "   SUM(o.total) as total, " +
                    "   COUNT(o.id) as countOrder " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u on o.user_id = u.id " +
                    "WHERE 1=1 ";

            String sqlCountTopSales = "SELECT COUNT(u.id), SUM(o.total) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u on o.user_id = u.id " +
                    "WHERE 1=1 ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlTopSales += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
                sqlCountTopSales += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlTopSales += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
                sqlCountTopSales += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            sqlTopSales += "GROUP BY u.id, ui.image_path, u.full_name, u.tel " +
                    "ORDER BY SUM(o.total) DESC";

            Query query = em.createNativeQuery(sqlTopSales).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlTopSalesDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountTopSales).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlCountTopSalesDTO.class));

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
                queryCount.setParameter("startDate", startDate);
                queryCount.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
                queryCount.setParameter("search", "%" + search + "%");
            }

            query.setFirstResult((pageNo - 1) * pageSize);
            query.setMaxResults(pageSize);

            ResSqlCountTopSalesDTO resultCount = (ResSqlCountTopSalesDTO) queryCount.getSingleResult();

            resDto.setPageNo(pageNo);
            resDto.setPageSize(pageSize);
            resDto.setTotalPages((int) Math.ceil(resultCount.getCount().intValue() / (pageSize + 0.0)));
            resDto.setTotalItems(resultCount.getCount().intValue());
            resDto.setItems(query.getResultList()); // TODO: เหลิือทำ total เป็น %

        } catch (Exception e) {
            logger.info("Can't getTopSalesBySearch: {}", e.getMessage());
        }

        return resDto;
    }

    private List<ResSqlTopSalesDTO> mapPercentAndImageToResultTopSales(List<ResSqlTopSalesDTO> resSqlTopSalesDTOS, double total) {
        List<ResSqlTopSalesDTO> results = new ArrayList<>();

        if (resSqlTopSalesDTOS.size() > 0) {
            List<Integer> userIds = resSqlTopSalesDTOS.stream().map(ResSqlTopSalesDTO::getUserid).collect(Collectors.toList());
            List<UserImage> userImages = userImageRepository.findAllProfileByUserIds(userIds);

            for (int i=0 ; i<resSqlTopSalesDTOS.size() ; i++) {
                ResSqlTopSalesDTO resDTO = resSqlTopSalesDTOS.get(i);
                double eachTotal = resDTO.getTotal().doubleValue();
                resDTO.setPercent(eachTotal / total * 100);

                // Find image
                userImages.stream()
                        .filter(image -> image.getUserId() == resDTO.getUserid())
                        .findFirst()
                        .ifPresent(userImage -> resDTO.setImagepath(userImage.getImagePath()));

                results.add(resDTO);
            }
        }

        return results;
    }

}

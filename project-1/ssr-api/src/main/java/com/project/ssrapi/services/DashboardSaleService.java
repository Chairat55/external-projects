package com.project.ssrapi.services;

import com.example.shinsiri.dtos.res.dashboard.*;
import com.example.shinsiri.entities.Order;
import com.example.shinsiri.repositories.OrderRepository;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class DashboardSaleService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardSaleService.class);

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public OverviewSaleDTO getOverview(int userId, String search, String startDate, String endDate) {
        OverviewSaleDTO overviewSaleDTO = new OverviewSaleDTO();

        Calendar now = Calendar.getInstance();
        String currentDay = String.format("%02d", now.get(Calendar.DAY_OF_MONTH));
        String currentMonth = String.format("%02d", now.get(Calendar.MONTH) + 1);
        String currentYear = String.valueOf(now.get(Calendar.YEAR));

        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            getMonthlySales(userId, currentMonth, currentYear, overviewSaleDTO);
            getMeetCustomers(userId, currentDay, firstDayOfThisMonth, overviewSaleDTO);
            getOrders(userId, currentDay, firstDayOfThisMonth, overviewSaleDTO);
            getCountOrderByStatus(userId, firstDayOfThisMonth, overviewSaleDTO);
        } else {
            getMonthlySalesBySearch(userId, search, startDate, endDate, overviewSaleDTO);
            getMeetCustomersBySearch(userId, search, startDate, endDate, overviewSaleDTO);
            getOrdersBySearch(userId, search, startDate, endDate, overviewSaleDTO);
            getCountOrderByStatusBySearch(userId, search, startDate, endDate, overviewSaleDTO);
        }

        return overviewSaleDTO;
    }

    public ResTopProductsDTO topProducts(int userId, String search, String startDate, String endDate, int pageNo, int pageSize) {
        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            return getTopProducts(userId, firstDayOfThisMonth, pageNo, pageSize);
        } else {
            return getTopProductsBySearch(userId, search, startDate, endDate, pageNo, pageSize);
        }
    }

    public ResNewCustomersDTO newCustomers(int userId, String search, String startDate, String endDate, int pageNo, int pageSize) {
        Calendar firstDayOfThisMonth = Calendar.getInstance();
        firstDayOfThisMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (search.equals("") && startDate.equals("") && endDate.equals("")) {
            return getNewCustomers(userId, firstDayOfThisMonth, pageNo, pageSize);
        } else {
            return getNewCustomersBySearch(userId, search, startDate, endDate, pageNo, pageSize);
        }
    }

    private void getMonthlySales(int userId, String currentMonth, String currentYear, OverviewSaleDTO overviewSaleDTO) {
        try {
            // เดือน 1 ไม่ต้อง compare lastMonth
            boolean isJan = Integer.parseInt(currentMonth) == 1;

            String sqlMonthlySales = "SELECT TO_CHAR(o.created_date , 'MM') AS month, SUM(o.net) AS total " +
                    "FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE TO_CHAR(o.created_date , 'YYYY') = :currentYear " +
                    "   AND u.id = :userId " +
                    "GROUP BY TO_CHAR(o.created_date , 'MM')" +
                    "ORDER BY to_char(o.created_date , 'MM') DESC ";

            Query query = em.createNativeQuery(sqlMonthlySales).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlMonthlyDTO.class));

            query.setParameter("currentYear", currentYear);
            query.setParameter("userId", userId);

            List<ResSqlMonthlyDTO> monthlySaleResults = query.getResultList();

            // currentMonthSale & lastMonthSale
            for (int i = 1; i <= (isJan ? 1 : 2); i++) {
                String month = isJan ? currentMonth : (i == 1 ? currentMonth : String.format("%02d", Integer.parseInt(currentMonth) - 1));

                ResSqlMonthlyDTO findByMonth = monthlySaleResults.stream()
                        .filter(monthlySaleResult -> monthlySaleResult.getMonth().equals(month))
                        .findFirst()
                        .orElse(null);

                Double totalThisMonth = findByMonth == null ? (i > Integer.parseInt(month) ? null : 0.0) : Double.valueOf(findByMonth.getTotal().doubleValue());

                if (isJan) {
                    overviewSaleDTO.setCurrentMonthSale(totalThisMonth);
                } else {
                    if (i == 1) {
                        overviewSaleDTO.setCurrentMonthSale(totalThisMonth);
                    } else {
                        overviewSaleDTO.setLastMonthSale(totalThisMonth);
                    }
                }
            }

            // CompareLastMonth sale
            int compareSaleLastMonth;
            if (overviewSaleDTO.getLastMonthSale() != null && overviewSaleDTO.getLastMonthSale() != 0.0) {

                if (overviewSaleDTO.getLastMonthSale() > overviewSaleDTO.getCurrentMonthSale()) {
                    compareSaleLastMonth = (int) (
                            overviewSaleDTO.getCurrentMonthSale()
                                    / overviewSaleDTO.getLastMonthSale() * 100 - 100
                    );

                } else {
                    compareSaleLastMonth = (int) (
                            (overviewSaleDTO.getCurrentMonthSale() - overviewSaleDTO.getLastMonthSale())
                                    / overviewSaleDTO.getLastMonthSale() * 100
                    );
                }

                OverviewSaleDTO.CompareLastMonth compareLastMonth = overviewSaleDTO.getCompareLastMonth();
                compareLastMonth.setSale(compareSaleLastMonth);

                overviewSaleDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getMonthlySales: {}", e.getMessage());
        }
    }

    private void getMonthlySalesBySearch(int userId, String search, String startDate, String endDate, OverviewSaleDTO overviewSaleDTO) {
        try {
            String sqlMonthlySalesBySearch = "SELECT SUM(o.net) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE u.id = :userId ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlMonthlySalesBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlMonthlySalesBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlMonthlySalesBySearch);
            query.setParameter("userId", userId);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            double totalSale = ((BigDecimal) query.getSingleResult()).doubleValue();

            overviewSaleDTO.setTotalSale(totalSale);

        } catch (Exception e) {
            logger.info("Can't getMonthlySales: {}", e.getMessage());
        }
    }

    private void getMeetCustomers(int userId, String currentDay, Calendar firstDayOfThisMonth, OverviewSaleDTO overviewSaleDTO) {
        try {
            String sqlDailyMeetNewCustomers = "SELECT TO_CHAR(c.created_date , 'DD') as day, COUNT(c.id) as count " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE c.created_date >= :firstDayOfThisMonth " +
                    "   AND u.id = :userId " +
                    "GROUP BY TO_CHAR(c.created_date , 'DD') " +
                    "ORDER BY TO_CHAR(c.created_date , 'DD') ";

            String sqlDailyMeetOldCustomers = "SELECT TO_CHAR(c.created_date , 'DD') as day, COUNT(c.id) as count " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE c.created_date < :firstDayOfThisMonth " +
                    "   AND u.id = :userId " +
                    "GROUP BY TO_CHAR(c.created_date , 'DD') " +
                    "ORDER BY TO_CHAR(c.created_date , 'DD') ";

            Query queryNew = em.createNativeQuery(sqlDailyMeetNewCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            Query queryOld = em.createNativeQuery(sqlDailyMeetOldCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            queryNew.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryNew.setParameter("userId", userId);
            queryOld.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryOld.setParameter("userId", userId);

            List<Integer> dailyMeetNewCustomers = new ArrayList<>();
            List<Integer> dailyMeetOldCustomers = new ArrayList<>();
            List<ResSqlDailyDTO> dailyMeetNewCustomerResults = queryNew.getResultList();
            List<ResSqlDailyDTO> dailyMeetOldCustomerResults = queryOld.getResultList();

            // totalOldCustomers, totalNewCustomers
            overviewSaleDTO.setTotalNewCustomers(dailyMeetNewCustomerResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());
            overviewSaleDTO.setTotalOldCustomers(dailyMeetOldCustomerResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());

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

            overviewSaleDTO.setDailyMeetNewCustomers(dailyMeetNewCustomers);
            overviewSaleDTO.setDailyMeetOldCustomers(dailyMeetOldCustomers);

//          // CompareLastMonth meetCustomer
            int compareMeetCustomerLastMonth;
            if (overviewSaleDTO.getTotalOldCustomers() != 0) {

                if (overviewSaleDTO.getTotalOldCustomers() > overviewSaleDTO.getTotalNewCustomers()) {
                    compareMeetCustomerLastMonth = overviewSaleDTO.getTotalNewCustomers()
                            / overviewSaleDTO.getTotalOldCustomers() * 100 - 100;

                } else {
                    compareMeetCustomerLastMonth = (overviewSaleDTO.getTotalNewCustomers() - overviewSaleDTO.getTotalOldCustomers())
                            / overviewSaleDTO.getTotalOldCustomers() * 100;
                }

                OverviewSaleDTO.CompareLastMonth compareLastMonth = overviewSaleDTO.getCompareLastMonth();
                compareLastMonth.setMeetCustomer(compareMeetCustomerLastMonth);

                overviewSaleDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getMeetCustomers: {}", e.getMessage());
        }
    }

    private void getMeetCustomersBySearch(int userId, String search, String startDate, String endDate, OverviewSaleDTO overviewSaleDTO) {
        try {
            String sqlNewCustomersBySearch = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE u.id = :userId ";

            String sqlAllCustomer = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE u.id = :userId ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlNewCustomersBySearch += "    AND TO_CHAR(c.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlNewCustomersBySearch += "    OR c.full_name LIKE :search ";
            }

            Query queryBySearch = em.createNativeQuery(sqlNewCustomersBySearch);
            Query queryAll = em.createNativeQuery(sqlAllCustomer);

            queryBySearch.setParameter("userId", userId);
            queryAll.setParameter("userId", userId);

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
            overviewSaleDTO.setTotalNewCustomers(totalBySearch);
            overviewSaleDTO.setTotalOldCustomers(totalAll - totalBySearch);

        } catch (Exception e) {
            logger.info("Can't getMeetCustomersBySearch: {}", e.getMessage());
        }
    }

    private void getOrders(int userId, String currentDay, Calendar firstDayOfThisMonth, OverviewSaleDTO overviewSaleDTO) {
        try {
            Calendar firstDayOfLastMonth = Calendar.getInstance();
            firstDayOfLastMonth.add(Calendar.MONTH, -1);
            firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

            // Query daily current month
            String sqlDailyOrders = "SELECT TO_CHAR(o.created_date , 'DD') as day, COUNT(o.id) as count " +
                    "FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.created_date >= :firstDayOfThisMonth " +
                    "   AND u.id = :userId " +
                    "GROUP BY TO_CHAR(o.created_date , 'DD') " +
                    "ORDER BY TO_CHAR(o.created_date , 'DD') ";

            Query query = em.createNativeQuery(sqlDailyOrders).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            query.setParameter("userId", userId);

            // Query last month เพื่อเอามา compare
            String sqlLastMonthOrders = "SELECT TO_CHAR(o.created_date , 'DD') as day, COUNT(o.id) as count " +
                    "FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.created_date >= :firstDayOfLastMonth " +
                    "   AND o.created_date < :firstDayOfThisMonth " +
                    "   AND u.id = :userId " +
                    "GROUP BY TO_CHAR(o.created_date , 'DD') " +
                    "ORDER BY TO_CHAR(o.created_date , 'DD') ";

            Query queryLastMonth = em.createNativeQuery(sqlLastMonthOrders).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlDailyDTO.class));

            queryLastMonth.setParameter("firstDayOfLastMonth", firstDayOfLastMonth);
            queryLastMonth.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryLastMonth.setParameter("userId", userId);

            List<Integer> dailyOrders = new ArrayList<>();
            List<ResSqlDailyDTO> dailyOrderResults = query.getResultList();
            List<ResSqlDailyDTO> dailyLastMonthOrderResults = queryLastMonth.getResultList();

            // totalOrders
            overviewSaleDTO.setTotalOrders(dailyOrderResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum());

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

            overviewSaleDTO.setDailyOrders(dailyOrders);

            // CompareLastMonth order
            int compareOrderLastMonth;
            if (dailyLastMonthOrderResults.size() > 0) {

                int countLastMonthOrder = dailyOrderResults.stream().map(d -> d.getCount().intValue()).mapToInt(Integer::intValue).sum();

                if (countLastMonthOrder > overviewSaleDTO.getTotalOrders()) {
                    compareOrderLastMonth = overviewSaleDTO.getTotalOrders()
                            / countLastMonthOrder * 100 - 100;

                } else {
                    compareOrderLastMonth = (overviewSaleDTO.getTotalOrders() - countLastMonthOrder)
                            / countLastMonthOrder * 100;
                }

                OverviewSaleDTO.CompareLastMonth compareLastMonth = overviewSaleDTO.getCompareLastMonth();
                compareLastMonth.setOrder(compareOrderLastMonth);

                overviewSaleDTO.setCompareLastMonth(compareLastMonth);
            }

        } catch (Exception e) {
            logger.info("Can't getOrders: {}", e.getMessage());
        }
    }

    private void getOrdersBySearch(int userId, String search, String startDate, String endDate, OverviewSaleDTO overviewSaleDTO) {
        try {
            String sqlOrdersBySearch = "SELECT COUNT(o.id) " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE u.id = :userId ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlOrdersBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlOrdersBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlOrdersBySearch);
            query.setParameter("userId", userId);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            // totalOrders
            overviewSaleDTO.setTotalOrders(((BigInteger) query.getSingleResult()).intValue());

        } catch (Exception e) {
            logger.info("Can't getOrdersBySearch: {}", e.getMessage());
        }
    }

    private void getCountOrderByStatus(int userId, Calendar firstDayOfThisMonth, OverviewSaleDTO overviewSaleDTO) {
        try {
            String sqlCountOrderByStatus = "SELECT o.* " +
                    "FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.created_date >= :firstDayOfThisMonth " +
                    "   AND u.id = :userId " +
                    "   AND o.status IN ('WAIT_LICENSE_1', 'WAIT_LICENSE_2', 'DELETE', 'SHIPPED') ";

            Query query = em.createNativeQuery(sqlCountOrderByStatus, Order.class);
            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            query.setParameter("userId", userId);

            List<Order> results = query.getResultList();

            if (results.size() > 0) {
                overviewSaleDTO.setCountSuccessOrder((int) results.stream().filter(r -> r.getStatus().equals("SHIPPED")).count());
                overviewSaleDTO.setCountDraftOrder((int) results.stream().filter(r -> r.getStatus().equals("WAIT_LICENSE_1") || r.getStatus().equals("WAIT_LICENSE_2")).count());
                overviewSaleDTO.setCountCancelOrder((int) results.stream().filter(r -> r.getStatus().equals("DELETE")).count());
            }

        } catch (Exception e) {
            logger.info("Can't getCountOrderByStatus: {}", e.getMessage());
        }
    }

    private void getCountOrderByStatusBySearch(int userId, String search, String startDate, String endDate, OverviewSaleDTO overviewSaleDTO) {
        try {
            String sqlCountOrderByStatusBySearch = "SELECT o.* " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE u.id = :userId " +
                    "   AND o.status IN ('WAIT_LICENSE_1', 'WAIT_LICENSE_2', 'DELETE', 'SHIPPED') ";

            if (!startDate.equals("") && !endDate.equals("")) {
                sqlCountOrderByStatusBySearch += "    AND TO_CHAR(o.created_date, 'YYYY-MM-DD') BETWEEN :startDate AND :endDate ";
            }
            if (!search.equals("")) {
                sqlCountOrderByStatusBySearch += "    AND (p.name LIKE :search OR c.full_name LIKE :search OR u.full_name LIKE :search)";
            }

            Query query = em.createNativeQuery(sqlCountOrderByStatusBySearch, Order.class);
            query.setParameter("userId", userId);

            if (!startDate.equals("") && !endDate.equals("")) {
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
            }
            if (!search.equals("")) {
                query.setParameter("search", "%" + search + "%");
            }

            List<Order> results = query.getResultList();

            if (results.size() > 0) {
                overviewSaleDTO.setCountSuccessOrder((int) results.stream().filter(r -> r.getStatus().equals("SHIPPED")).count());
                overviewSaleDTO.setCountDraftOrder((int) results.stream().filter(r -> r.getStatus().equals("WAIT_LICENSE_1") || r.getStatus().equals("WAIT_LICENSE_2")).count());
                overviewSaleDTO.setCountCancelOrder((int) results.stream().filter(r -> r.getStatus().equals("DELETE")).count());
            }

        } catch (Exception e) {
            logger.info("Can't getCountOrderByStatusBySearch: {}", e.getMessage());
        }
    }

    private ResTopProductsDTO getTopProducts(int userId, Calendar firstDayOfThisMonth, int pageNo, int pageSize) {
        ResTopProductsDTO resDto = new ResTopProductsDTO();

        try {
            String sqlTopProducts = "SELECT p.name, p.type, SUM(o.net) as total " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.created_date >= :firstDayOfThisMonth" +
                    "   AND u.id = :userId " +
                    "GROUP BY p.name, p.type " +
                    "ORDER BY SUM(o.net) DESC";

            String sqlCountTopProducts = "SELECT COUNT(*) " +
                    "FROM (" +
                    "   SELECT COUNT(p.name) " +
                    "   FROM orders o " +
                    "   JOIN order_details od ON o.id = od.order_id " +
                    "   JOIN products p ON od.product_id = p.id " +
                    "   JOIN users u ON o.user_id = u.id " +
                    "   WHERE o.created_date >= :firstDayOfThisMonth " +
                    "       AND u.id = :userId " +
                    "   GROUP BY p.name, p.type " +
                    ") as count ";

            Query query = em.createNativeQuery(sqlTopProducts).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlTopProductsDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountTopProducts);

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            query.setParameter("userId", userId);
            queryCount.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryCount.setParameter("userId", userId);

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

    private ResTopProductsDTO getTopProductsBySearch(int userId, String search, String startDate, String endDate, int pageNo, int pageSize) {
        ResTopProductsDTO resDto = new ResTopProductsDTO();

        try {
            String sqlTopProducts = "SELECT p.name, p.type, SUM(o.net) as total " +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE u.id = :userId ";

            String sqlCountTopProducts = "SELECT COUNT(*) " +
                    "FROM (" +
                    "   SELECT COUNT(p.name) " +
                    "   FROM orders o " +
                    "   JOIN order_details od ON o.id = od.order_id " +
                    "   JOIN products p ON od.product_id = p.id " +
                    "   JOIN customers c ON o.customer_id = c.id " +
                    "   JOIN users u ON o.user_id = u.id " +
                    "   WHERE u.id = :userId ";


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
            query.setParameter("userId", userId);

            Query queryCount = em.createNativeQuery(sqlCountTopProducts);
            queryCount.setParameter("userId", userId);

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

    private ResNewCustomersDTO getNewCustomers(int userId, Calendar firstDayOfThisMonth, int pageNo, int pageSize) {
        ResNewCustomersDTO resDto = new ResNewCustomersDTO();

        try {
            String sqlNewCustomers = "SELECT c.full_name as fullName " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE c.created_date >= :firstDayOfThisMonth " +
                    "   AND u.id = :userId " +
                    "ORDER BY c.created_date DESC ";

            String sqlCountNewCustomers = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE c.created_date >= :firstDayOfThisMonth " +
                    "   AND u.id = :userId ";

            Query query = em.createNativeQuery(sqlNewCustomers).unwrap(org.hibernate.query.Query.class)
                    .setResultTransformer(new AliasToBeanResultTransformer(ResSqlNewCustomersDTO.class));

            Query queryCount = em.createNativeQuery(sqlCountNewCustomers);

            query.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            query.setParameter("userId", userId);
            queryCount.setParameter("firstDayOfThisMonth", firstDayOfThisMonth);
            queryCount.setParameter("userId", userId);

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

    private ResNewCustomersDTO getNewCustomersBySearch(int userId, String search, String startDate, String endDate, int pageNo, int pageSize) {
        ResNewCustomersDTO resDto = new ResNewCustomersDTO();

        try {
            String sqlNewCustomers = "SELECT c.full_name as fullName " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE u.id = :userId ";

            String sqlCountNewCustomers = "SELECT COUNT(c.id) " +
                    "FROM customers c " +
                    "JOIN users u ON c.user_id = u.id " +
                    "WHERE u.id = :userId ";

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
            query.setParameter("userId", userId);

            Query queryCount = em.createNativeQuery(sqlCountNewCustomers);
            queryCount.setParameter("userId", userId);

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

}

package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.order.ReqCreateOrderDTO;
import com.example.shinsiri.dtos.req.order.ReqOrderCalculateDTO;
import com.example.shinsiri.dtos.req.order.ReqSearchOrderDTO;
import com.example.shinsiri.dtos.req.order.ReqUpdateOrderStatusDTO;
import com.example.shinsiri.dtos.res.order.ResOrderCalculateDTO;
import com.example.shinsiri.dtos.res.order.ResOrderDTO;
import com.example.shinsiri.dtos.res.order.ResSearchOrderDTO;
import com.example.shinsiri.entities.*;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private static final List<String> ORDER_STATUSES = Arrays.asList("WAIT_LICENSE_1", "WAIT_LICENSE_2", "WAIT_APPROVE", "REJECT", "DELETE", "PREPARE", "SHIPPING", "SHIPPED");
    private static final String WAIT_LICENSE_1_STATUS = "WAIT_LICENSE_1";
    public static final String WAIT_LICENSE_2_STATUS = "WAIT_LICENSE_2";
    public static final String WAIT_APPROVE_STATUS = "WAIT_APPROVE";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderImageRepository orderImageRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public ResOrderDTO getOrderById(int orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new BadRequestException("orderId: " + orderId + " ไม่มีในระบบ");
        }

        ResOrderDTO resDto = new ResOrderDTO();
        modelMapper.map(order.get(), resDto);

        resDto.setCustomer(customerRepository.findById(resDto.getCustomerId()).orElse(null));
        resDto.setUser(userRepository.findById(resDto.getUserId()).orElse(null));
        resDto.setOrderImages(orderImageRepository.findAllByOrderId(orderId));

        // orderDetails ต้องส่ง product detail ไปด้วย
        List<OrderDetail> resOrderDetails = new ArrayList<>();

        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderId(orderId);
        if (!orderDetails.isEmpty()) {
            List<Integer> productIds = orderDetails.stream().map(OrderDetail::getProductId).collect(Collectors.toList());
            List<Product> products = productRepository.findAllByIds(productIds);

            if (productIds.size() != products.size()) {
                throw new BadRequestException("สินค้าบางรายการไม่มีในระบบ");
            }

            for (OrderDetail orderDetail : orderDetails) {
                Product product = products.stream().filter(pro -> pro.getId() == orderDetail.getProductId()).findFirst().orElse(null);
                if (product != null) {
                    orderDetail.setProductName(product.getName());
                    orderDetail.setCode(product.getCode());
                    orderDetail.setType(product.getType());
                }
                resOrderDetails.add(orderDetail);
            }
        }

        resDto.setOrderDetails(resOrderDetails);

        return resDto;
    }

    public ResSearchOrderDTO searchOrder(ReqSearchOrderDTO dto, User user) {
        int userId = user.getId();
        String role = user.getRoles().stream().map(Role::getName).findFirst().orElse(null);

        int pageNo = dto.getPageNo() == null ? 1 : dto.getPageNo();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        StringBuilder sql = new StringBuilder(
                "SELECT o.* " +
                "FROM orders o " +
                "JOIN order_details od ON o.id = od.order_id " +
                "JOIN products p ON od.product_id = p.id " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN customers c ON o.customer_id = c.id " +
                "WHERE 1=1 "
        );
        StringBuilder sqlCount = new StringBuilder(
                "SELECT COUNT(o.id) " +
                "FROM orders o " +
                "JOIN order_details od ON o.id = od.order_id " +
                "JOIN products p ON od.product_id = p.id " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN customers c ON o.customer_id = c.id " +
                "WHERE 1=1 "
        );
        StringBuilder sqlCountAll = new StringBuilder("SELECT COUNT(id) FROM orders WHERE 1=1 ");

        sqlOrderCondition(sql, sqlCount, sqlCountAll, dto, role);

        Query query = em.createNativeQuery(sql.toString(), Order.class);
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query queryCountAll = em.createNativeQuery(sqlCountAll.toString());

        queryOrderCondition(query, queryCount, queryCountAll, dto, userId, role);

        query.setFirstResult((pageNo - 1) * pageSize);
        query.setMaxResults(pageSize);

        int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();
        int totalAll = ((BigInteger) queryCountAll.getSingleResult()).intValue();

        List<ResOrderDTO> mapResult = mapOrderListToResOrderDTOList(query.getResultList());

        ResSearchOrderDTO resDto = new ResSearchOrderDTO();
        resDto.setPageNo(pageNo);
        resDto.setPageSize(pageSize);
        resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
        resDto.setTotalItems(totalItems);
        resDto.setTotalAll(totalAll);
        resDto.setItems(mapResult);

        return resDto;
    }

    private void sqlOrderCondition(
            StringBuilder sql,
            StringBuilder sqlCount,
            StringBuilder sqlCountAll,
            ReqSearchOrderDTO dto,
            String role
    ) {
        if (dto.getSearch() != null && !dto.getSearch().equals("")) {
            sql.append("    AND (c.full_name LIKE :search OR p.name LIKE :search) ");
            sqlCount.append("    AND (c.full_name LIKE :search OR p.name LIKE :search) ");
        }
        if (dto.getProvince() != null && !dto.getProvince().equals("")) {
            sql.append("    AND o.delivery_province = :province ");
            sqlCount.append("    AND o.delivery_province = :province ");
        }
        if (dto.getProductName() != null && !dto.getProductName().equals("")) {
            sql.append("    AND p.name = :productName ");
            sqlCount.append("    AND p.name = :productName ");
        }
        if (dto.getStatus().size() > 0) {
            sql.append("    AND o.status IN :status ");
            sqlCount.append("    AND o.status IN :status ");
        }
        if (dto.getOrderStartDate() != null && dto.getOrderEndDate() != null) {
            sql.append("    AND o.created_date >= :startDate AND o.created_date <= :endDate ");
            sqlCount.append("    AND o.created_date >= :startDate AND o.created_date <= :endDate ");
        }
        if (dto.getCustomerType() != null && !dto.getCustomerType().equals("")) {
            sql.append("    AND c.type = :customerType ");
            sqlCount.append("    AND c.type = :customerType ");
        }
        if (!ROLE_MANAGER.equals(role) && !ROLE_SUPER_ADMIN.equals(role)) {
            sql.append("    AND o.user_id = :userId AND o.status <> 'DELETE' ");
            sqlCount.append("    AND o.user_id = :userId AND o.status <> 'DELETE' ");
            sqlCountAll.append("    AND user_id = :userId AND status <> 'DELETE' ");

        } else if (dto.getUserId() != null) {
            sql.append("    AND o.user_id = :userId ");
            sqlCount.append("    AND o.user_id = :userId ");
            sqlCountAll.append("    AND user_id = :userId ");
        }
        if (dto.getCustomerId() != null) {
            sql.append("    AND o.customer_id = :customerId ");
            sqlCount.append("    AND o.customer_id = :customerId ");
            sqlCountAll.append("    AND customer_id = :customerId ");
        }
    }

    private void queryOrderCondition(
            Query query,
            Query queryCount,
            Query queryCountAll,
            ReqSearchOrderDTO dto,
            int userId,
            String role
    ) {
        if (dto.getSearch() != null && !dto.getSearch().equals("")) {
            query.setParameter("search", "%" + dto.getSearch() + "%");
            queryCount.setParameter("search", "%" + dto.getSearch() + "%");
        }
        if (dto.getProvince() != null && !dto.getProvince().equals("")) {
            query.setParameter("province", "%" + dto.getProvince() + "%");
            queryCount.setParameter("province", "%" + dto.getProvince() + "%");
        }
        if (dto.getProductName() != null && !dto.getProductName().equals("")) {
            query.setParameter("productName", dto.getProductName());
            queryCount.setParameter("productName", dto.getProductName());
        }
        if (dto.getStatus().size() > 0) {
            query.setParameter("status", dto.getStatus());
            queryCount.setParameter("status", dto.getStatus());
        }
        if (dto.getOrderStartDate() != null && dto.getOrderEndDate() != null) {
            query.setParameter("startDate", dto.getOrderStartDate());
            queryCount.setParameter("startDate", dto.getOrderStartDate());
            query.setParameter("endDate", dto.getOrderEndDate());
            queryCount.setParameter("endDate", dto.getOrderEndDate());
        }
        if (dto.getCustomerType() != null && !dto.getCustomerType().equals("")) {
            query.setParameter("customerType", dto.getCustomerType());
            queryCount.setParameter("customerType", dto.getCustomerType());
        }
        if (!ROLE_MANAGER.equals(role) && !ROLE_SUPER_ADMIN.equals(role)) {
            query.setParameter("userId", userId);
            queryCount.setParameter("userId", userId);
            queryCountAll.setParameter("userId", userId);

        } else if (dto.getUserId() != null) {
            query.setParameter("userId", dto.getUserId());
            queryCount.setParameter("userId", dto.getUserId());
            queryCountAll.setParameter("userId", dto.getUserId());
        }

        if (dto.getCustomerId() != null) {
            query.setParameter("customerId", dto.getCustomerId());
            queryCount.setParameter("customerId", dto.getCustomerId());
            queryCountAll.setParameter("customerId", dto.getCustomerId());
        }
    }

    private List<ResOrderDTO> mapOrderListToResOrderDTOList(List<Order> orders) {
        List<ResOrderDTO> resDtoList = new ArrayList<>();

        List<Integer> customerIds = orders.stream().map(Order::getCustomerId).collect(Collectors.toList());
        List<Integer> userIds = orders.stream().map(Order::getUserId).collect(Collectors.toList());
        List<Integer> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        List<Customer> customers = customerRepository.findAllByCustomerIds(customerIds);
        List<User> users = userRepository.findAllByUserIds(userIds);
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderIds(orderIds);
        List<OrderImage> orderImages = orderImageRepository.findAllByOrderIds(orderIds);

        for (Order order : orders) {
            ResOrderDTO resDto = new ResOrderDTO();
            modelMapper.map(order, resDto);

            resDto.setCustomer(
                    customers.stream()
                            .filter(c -> c.getId() == order.getCustomerId())
                            .findFirst().orElse(null)
            );
            resDto.setUser(
                    users.stream()
                            .filter(u -> u.getId() == order.getUserId())
                            .findFirst().orElse(null)
            );
            resDto.setOrderDetails(
                    orderDetails.stream()
                            .filter(od -> od.getOrderId() == order.getId())
                            .collect(Collectors.toList())
            );
            resDto.setOrderImages(
                    orderImages.stream()
                            .filter(oi -> oi.getOrderId() == order.getId())
                            .collect(Collectors.toList())
            );

            resDtoList.add(resDto);
        }

        return resDtoList;
    }

    public ResOrderCalculateDTO getOrderCalculate(ReqOrderCalculateDTO dto) {
        ResOrderCalculateDTO resDto = new ResOrderCalculateDTO();

        double orderTotal = 0;
        double orderDiscount = 0;

        if (!dto.getOrderDetails().isEmpty()) {
            List<Integer> productIds = dto.getOrderDetails().stream().map(OrderDetail::getProductId).collect(Collectors.toList());
            List<Product> products = productRepository.findAllByIds(productIds);

            if (productIds.size() != products.size()) {
                throw new BadRequestException("สินค้าบางรายการไม่มีในระบบ");
            }

            for (OrderDetail orderDetail : dto.getOrderDetails()) {
                double price = products.stream().filter(product -> product.getId() == orderDetail.getProductId()).findFirst().get().getPrice();
                double total = price * orderDetail.getQuantity();

                orderDetail.setPrice(price);
                orderDetail.setTotal(total);

                orderTotal += total;

                // TODO: Discount เอามาใช้จังหวะไหน ?
            }

            resDto.setTotal(orderTotal);
            resDto.setDiscount(orderDiscount);
            resDto.setTotal(orderTotal - orderDiscount);
        }

        return resDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(ReqCreateOrderDTO dto, int userId) {
        Order order = new Order();

        try {
            modelMapper.map(dto, order);
            order.setUserId(userId);
            order.setStatus(WAIT_LICENSE_1_STATUS);
            orderRepository.saveAndFlush(order);

            updateOrderDetail("CREATE", dto.getOrderDetails(), order);

        } catch (Exception e) {
            logger.info("Can't create order");
            e.printStackTrace();
        }

        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order updateOrder(int orderId, ReqCreateOrderDTO dto) {
        Order order = orderRepository.findOneById(orderId);
        if (order == null) {
            throw new BadRequestException("orderId: " + orderId + " ไม่มีในระบบ");
        }

        try {
            modelMapper.map(dto, order);
            order.setStatus(WAIT_APPROVE_STATUS); // ถ้าเส้นนี้มีการเรียก จะ update status เป็น WAIT_APPROVE เสมอ
            orderRepository.saveAndFlush(order);

            updateOrderDetail("UPDATE", dto.getOrderDetails(), order);

        } catch (Exception e) {
            logger.info("Can't create order");
            e.printStackTrace();
        }

        return order;
    }

    private void updateOrderDetail(String activityType, List<OrderDetail> orderDetails, Order order) {
        double orderTotal = 0;
        double orderDiscount = 0;

        if (!orderDetails.isEmpty()) {
            // Get All Product Detail
            List<Integer> productIds = orderDetails.stream().map(OrderDetail::getProductId).collect(Collectors.toList());
            List<Product> products = productRepository.findAllByIds(productIds);

            if (productIds.size() != products.size()) {
                throw new BadRequestException("สินค้าบางรายการไม่มีในระบบ");
            }

            for (OrderDetail orderDetail : orderDetails) {
                double price = products.stream().filter(product -> product.getId() == orderDetail.getProductId()).findFirst().get().getPrice();
                double total = price * orderDetail.getQuantity();

                orderDetail.setOrderId(order.getId());
                orderDetail.setPrice(price);
                orderDetail.setTotal(total);

                orderTotal += total;

                // TODO: Discount เอามาใช้จังหวะไหน ?
            }

            if (activityType.equals("UPDATE")) {
                orderDetailRepository.deleteAll(orderDetailRepository.findAllByOrderId(order.getId()));
            }
            orderDetailRepository.saveAll(orderDetails);
        }

        order.setTotal(orderTotal);
        order.setNet(orderTotal - orderDiscount);
        orderRepository.saveAndFlush(order);
    }

    public Order updateOrderStatus(int orderId, ReqUpdateOrderStatusDTO dto) {
        validateOrderStatus(dto.getStatus());

        Order order = orderRepository.findOneById(orderId);
        if (order == null) {
            throw new BadRequestException("orderId: " + orderId + " ไม่มีในระบบ");
        }

        order.setStatus(dto.getStatus());
        order.setRejectRemark(dto.getRejectRemark());
        order.setRejectDueDate(dto.getRejectDueDate());
        orderRepository.saveAndFlush(order);
        return order;
    }

    private void validateOrderStatus(String status) {
        if (!ORDER_STATUSES.contains(status)) {
            throw new BadRequestException("Status ต้องเป็นอยู่ในรายการนี้เท่านั้น " + String.join(", ", ORDER_STATUSES));
        }
    }

}

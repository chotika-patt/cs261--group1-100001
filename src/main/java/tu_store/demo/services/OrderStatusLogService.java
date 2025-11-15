package tu_store.demo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.models.*;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.repositories.*;

@Service
public class OrderStatusLogService {

    @Autowired
    private OrderStatusLogRepository orderStatusLogRepository;

    public OrderStatusLog createOrderLog(Order order, OrderStatus prevStatus, OrderStatus newStatus){
        if(order == null || newStatus == null) return null;
        if(prevStatus == newStatus) return null;

        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(order);
        log.setOldStatus(prevStatus);
        log.setNewStatus(newStatus);
        log.setUpdatedAt(LocalDateTime.now());
        return orderStatusLogRepository.save(log);
    }

    public OrderStatusLog createOrderLog(Order order, OrderStatus newStatus){
        return createOrderLog(order, order.getStatus(), newStatus);
    }

    public List<OrderStatusLog> getLatest100Logs(){
        return orderStatusLogRepository.findTop100ByOrderByUpdatedAtDesc();
    }

    public List<OrderStatusLog> getLogsByOrderId(Long orderId){
        return orderStatusLogRepository.findAllByOrderOrderId(orderId);
    }

    // ⭐⭐ Buyer ดู only ของตัวเอง
    public List<OrderStatusLog> getLogsForBuyer(Long buyerId){
        return orderStatusLogRepository.findAllByOrderBuyerUserId(buyerId);
    }

    // ⭐⭐ Seller ดู only ของร้านตัวเอง
    public List<OrderStatusLog> getLogsForSeller(Long sellerId){
        return orderStatusLogRepository.findAllByOrderSellerUserId(sellerId);
    }
}


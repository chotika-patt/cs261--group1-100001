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

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;


    public OrderStatusLog createOrderLog(Order order, OrderStatus prevStatus, OrderStatus newStatus){
        if(order == null || newStatus == null) return null;
        if (prevStatus == newStatus) return null;

        OrderStatusLog orderLog = new OrderStatusLog();
        orderLog.setOrder(order);
        orderLog.setOldStatus(prevStatus);
        orderLog.setNewStatus(newStatus);
        orderLog.setUpdatedAt(LocalDateTime.now());

        orderStatusLogRepository.save(orderLog);

        return orderLog;
    }
    public OrderStatusLog createOrderLog(Order order, OrderStatus newStatus){
        if(order == null) return null;
        OrderStatus prevStatus = order.getStatus();

        return createOrderLog(order, prevStatus, newStatus);
    }

    public List<OrderStatusLog> getLatest100Logs(){
       return orderStatusLogRepository.findTop100ByOrderByUpdatedAtDesc();
    }

    public List<OrderStatusLog> getAllLogsByOrderId(Long id){
       return orderStatusLogRepository.findAllByOrderOrderId(id);
    }

    public List<OrderStatusLog> getAllLogsByUserId(Long id){
       return orderStatusLogRepository.findAllByOrderUserUserId(id);
    }
}

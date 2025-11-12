package tu_store.demo.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.*;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ShipmentTrackingService;
import tu_store.demo.services.CartService;
import tu_store.demo.services.UserService;

import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.CartDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/order")

public class OrderController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipmentTrackingService shipmentTrackingService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(HttpSession session){
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");
        
        orderService.checkoutByUserId(userId);

        return ResponseEntity.ok(getOrders(session));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, HttpSession session) {
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");
        
        orderService.cancelOrderByUserId(userId, orderId);

        return ResponseEntity.ok(getOrders(session));
    }

    @GetMapping("/getOrders")
    public ResponseEntity<?> getOrders(HttpSession session) {
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");

        return ResponseEntity.ok(orderService.createOrdersResponseByUserId(userId));
    }
    
    @PostMapping("/{orderId}/updateStatus")
    public ResponseEntity<?> updateStatus(@PathVariable Long orderId, HttpSession session) {
        Order order = orderService.getOrderById(orderId);

        if(order == null) return ResponseEntity.ok("Wrong order Id");

        orderService.updateStatus(order);

        return ResponseEntity.ok(getOrders(session));
    }

    @PostMapping("/{orderId}/stUpdateStatus")
    public ResponseEntity<?> stUpdateStatus(@PathVariable Long orderId, HttpSession session) {
        Order order = orderService.getOrderById(orderId);

        if(order == null) return ResponseEntity.ok("Wrong order Id");

        shipmentTrackingService.updateStatus(order);

        return ResponseEntity.ok(order.getShipmentTracking());
    }
}

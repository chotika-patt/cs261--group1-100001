package tu_store.demo.controllers;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tu_store.demo.dto.ClientOrderResponse;
import tu_store.demo.dto.ClientShipmentTrackingResponse;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Order;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ShipmentTrackingService;
import tu_store.demo.services.UserService;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired private UserService userService;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private ShipmentTrackingService shipmentTrackingService;

    @PostMapping("/draft")
    public ResponseEntity<OrderDraftResponse> createDraft(HttpSession session, @RequestBody(required = false) Object body){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");


        var cart = cartService.getOrCreateCartByUserId(userId);
        if(cart == null || cart.getItems() == null || cart.getItems().isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_CART", "ตะกร้าว่าง");


// service will validate product existence/status/stock
        var order = orderService.createOrder(cart);


        OrderDraftResponse resp = orderService.createOrderDraftResponse(order);


        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("")
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            HttpSession session
    ){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        Sort sort;
        try {
            sort = Sort.by(sortBy).descending();
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD REQUEST", "Invalid sort field");
        }

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<ClientOrderResponse> orders = orderService.getClientOrderPageResponseWithStatus(userId, status, pageable);

        if(orders.getContent().isEmpty()) return ResponseEntity.status(404).body("Order not found");

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(HttpSession session, @PathVariable Long id){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        ClientOrderResponse response = orderService.createClientOrderResponseByIdAndUserId(id, userId);
        if (response == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT FOUND", "Order Not Found");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<?> getOrderTrackingByOrderId(HttpSession session, @PathVariable Long id){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        ClientShipmentTrackingResponse response = orderService.getClientShipmentTrackingResponseByOrderIdAndUserId(id, userId);
        if (response == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT FOUND", "Order ShipmentTracking Not Found");
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/checkoutTest")
    public ResponseEntity<?> checkoutTest(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            HttpSession session
    ){
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");
        
        orderService.checkoutByUserId(userId);

        Sort sort = Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<ClientOrderResponse> orders = orderService.getClientOrderPageResponseWithStatus(userId, status, pageable);

        if(orders.isEmpty()) return ResponseEntity.status(404).body("Order not found");

        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/updateStatusTest")
    public ResponseEntity<?> updateStatusTest(
            @PathVariable Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            HttpSession session
    ){

        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");

        Order order = orderService.getOrderById(orderId);

        if(order == null) return ResponseEntity.ok("Wrong order Id");

        orderService.updateStatus(order);

        Sort sort = Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<ClientOrderResponse> orders = orderService.getClientOrderPageResponseWithStatus(userId, status, pageable);

        if(orders.isEmpty()) return ResponseEntity.status(404).body("Order not found");

        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/stUpdateStatusTest")
    public ResponseEntity<?> stUpdateStatusTest(@PathVariable Long orderId, HttpSession session) {
        Order order = orderService.getOrderById(orderId);

        if(order == null) return ResponseEntity.ok("Wrong order Id");

        shipmentTrackingService.updateStatus(order);

        return ResponseEntity.ok(order.getShipmentTracking());
    }
}

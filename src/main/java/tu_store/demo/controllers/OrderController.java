package tu_store.demo.controllers;


import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tu_store.demo.dto.BuyerOrderResponse;
import tu_store.demo.dto.ShipmentTrackingResponse;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.dto.SellerOrderResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Order;
import tu_store.demo.models.enums.OrderStatus;
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt:desc") String sort,
            @RequestParam(required = false) String dateRange,
            HttpSession session
    ){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        // Sort sort;
        // try {
        //     sort = Sort.by(sortBy).descending();
        // } catch (IllegalArgumentException e) {
        //     throw new ApiException(HttpStatus.BAD_REQUEST, "BAD REQUEST", "Invalid sort field");
        // }

        // Pageable pageable = PageRequest.of(page, pageSize, sort);
        // Page<BuyerOrderResponse> orders = orderService.getClientOrderPageResponseWithStatus(userId, status, pageable);


        // Convert OrderStatus
        OrderStatus orderStatusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatusEnum = OrderStatus.valueOf(status); // convert string -> enum
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid status value. Allowed: " + Arrays.toString(OrderStatus.values()));
            }
        }


        // Convert dateRange
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (dateRange != null) {
            String[] parts = dateRange.split(",");
            if (parts.length != 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid dateRange format. Use: yyyy-MM-dd,yyyy-MM-dd");
            }

            try {
                startDate = LocalDate.parse(parts[0]).atStartOfDay();
                endDate = LocalDate.parse(parts[1]).atTime(23, 59, 59);
            } catch (DateTimeParseException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid date format. Use: yyyy-MM-dd,yyyy-MM-dd");
            }
        }

        // Convert sort
        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort.contains(":")) {
            String[] parts = sort.split(":");
            sortField = parts[0];
            direction = parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        Sort sortObj;
        try {
            sortObj = Sort.by(direction, sortField);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid sort field");
        }

        Pageable pageable = PageRequest.of(page, pageSize, sortObj);
        Page<BuyerOrderResponse> orders = orderService.getClientOrderPageResponseWithFilters(userId, search, orderStatusEnum, startDate, endDate, pageable);
        if(orders.getContent().isEmpty()) return ResponseEntity.status(404).body("Order not found");

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(HttpSession session, @PathVariable Long id){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        BuyerOrderResponse response = orderService.createClientOrderResponseByIdAndUserId(id, userId);
        if (response == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT FOUND", "Order Not Found");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<?> getOrderTrackingByOrderId(HttpSession session, @PathVariable Long id){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        ShipmentTrackingResponse response = orderService.getClientShipmentTrackingResponseByOrderIdAndUserId(id, userId);
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
        Page<BuyerOrderResponse> orders = orderService.getClientOrderPageResponseWithStatus(userId, status, pageable);

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

        // Convert OrderStatus
        OrderStatus orderStatusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatusEnum = OrderStatus.valueOf(status); // convert string -> enum
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid status value. Allowed: " + Arrays.toString(OrderStatus.values()));
            }
        }

        orderService.updateStatusTest(order, orderStatusEnum);

        Sort sort = Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<BuyerOrderResponse> orders = orderService.getClientOrderPageResponseWithStatus(userId, status, pageable);

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

package tu_store.demo.controllers;


import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tu_store.demo.dto.BuyerOrderResponse;
import tu_store.demo.dto.ShipmentTrackingResponse;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.dto.SellerOrderResponse;
import tu_store.demo.dto.SellerOrderSummaryResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Order;
import tu_store.demo.models.UserRole;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ShipmentTrackingService;
import tu_store.demo.services.UserService;
import org.springframework.http.HttpStatus;



@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {
    @Autowired private UserService userService;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private ShipmentTrackingService shipmentTrackingService;

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

        if (!userService.isSellerById(userId)) throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE"
        , "User does not have seller permissions.");

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
        Page<SellerOrderResponse> orders = orderService.getSellerOrderPageResponseWithFilters(userId, search, orderStatusEnum, startDate, endDate, pageable);

        if(orders.getContent().isEmpty()) return ResponseEntity.status(404).body("Order not found");

        SellerOrderSummaryResponse summary = orderService.createSellerOrderSummaryResponseBySellerIdWithFilters(userId, search, orderStatusEnum, startDate, endDate);

        Page<SellerOrderResponse> pageWithContentOnly = new PageImpl<>(orders.getContent(), orders.getPageable(), orders.getTotalElements());


        Map<String, Object> response = new LinkedHashMap<>(); // ใช้ LinkedHashMap เพื่อเก็บลำดับ

        response.put("summary", summary);

        response.put("content", orders.getContent());

        response.put("pageable", orders.getPageable());
        response.put("last", orders.isLast());
        response.put("totalPages", orders.getTotalPages());
        response.put("totalElements", orders.getTotalElements());
        response.put("size", orders.getSize());
        response.put("number", orders.getNumber());
        response.put("sort", orders.getSort());
        response.put("first", orders.isFirst());
        response.put("numberOfElements", orders.getNumberOfElements());
        response.put("empty", orders.isEmpty());
    

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(HttpSession session, @PathVariable Long id){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        if (!userService.isSellerById(userId)) throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE"
        , "User does not have seller permissions.");

        SellerOrderResponse response = orderService.createSellerOrderResponseByIdAndUserId(id, userId);
        if (response == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT FOUND", "Order Not Found");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> setOrderStatus(HttpSession session, @PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        if (!userService.isSellerById(userId)) throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE"
        , "User does not have seller permissions.");

        if (!userService.isVerifiedSellerById(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "UNVERIFIED_SELLER", "Your account has not been verified yet.");
        }

        Order order = orderService.getOrderById(id);
        if(order == null) return ResponseEntity.status(404).body("Order not found");

        String status = body.get("newStatus");
        if (status == null || status.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_NEW_STATUS", "Missing newStatus");
        }

        ShipmentTrackingStatus orderSTStatusEnum = null;
        try {
            orderSTStatusEnum = ShipmentTrackingStatus.valueOf(status); // convert string -> enum
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Invalid status value. Allowed: " + Arrays.toString(ShipmentTrackingStatus.values()));
        }
        
        if(orderService.updateStatus(order, orderSTStatusEnum) == false) throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "INVALID_STATUS_FLOW",
            "Cannot change order status from " + order.getShipmentTracking().getStatus() + " to " + orderSTStatusEnum
        );

        return ResponseEntity.ok(shipmentTrackingService.createShipmentTrackingResponse(order.getShipmentTracking()));
    }

}

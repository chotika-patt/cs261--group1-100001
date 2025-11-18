package tu_store.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tu_store.demo.dto.SellerOrderResponse;
import tu_store.demo.dto.SellerOrderSummaryResponse;
import tu_store.demo.dto.ShipmentTrackingResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Order;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ShipmentTrackingService;
import tu_store.demo.services.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    @Autowired private UserService userService;
    @Autowired private OrderService orderService;
    @Autowired private ShipmentTrackingService shipmentTrackingService;

    @GetMapping("")
    public ResponseEntity<?> listOrders(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt:desc") String sort
    ) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");
        if (!userService.isSellerById(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE", "User does not have seller permissions.");

        // status -> OrderStatus
        OrderStatus orderStatusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatusEnum = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid status. Allowed: " + Arrays.toString(OrderStatus.values()));
            }
        }

        // dateRange -> LocalDateTime
        LocalDateTime startDate = null, endDate = null;
        if (dateRange != null && !dateRange.isBlank()) {
            String[] parts = dateRange.split(",");
            if (parts.length != 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid dateRange format. Use: yyyy-MM-dd,yyyy-MM-dd");
            }
            try {
                startDate = LocalDate.parse(parts[0].trim()).atStartOfDay();
                endDate = LocalDate.parse(parts[1].trim()).atTime(23, 59, 59);
            } catch (DateTimeParseException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid date format in dateRange. Use: yyyy-MM-dd,yyyy-MM-dd");
            }
        }

        // sort
        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && sort.contains(":")) {
            String[] sp = sort.split(":");
            sortField = sp[0].trim();
            direction = sp[1].trim().equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Sort sortObj;
        try {
            sortObj = Sort.by(direction, sortField);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Invalid sort field: " + sortField);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, pageSize), sortObj);

        Page<SellerOrderResponse> pageResult =
                orderService.getSellerOrderPageResponseWithFilters(
                        userId, search, orderStatusEnum, startDate, endDate, pageable);

        SellerOrderSummaryResponse summary =
                orderService.createSellerOrderSummaryResponseBySellerIdWithFilters(
                        userId, search, orderStatusEnum, startDate, endDate);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("summary", summary);
        resp.put("content", pageResult.getContent());

        Map<String, Object> pageableMeta = new LinkedHashMap<>();
        pageableMeta.put("page", pageResult.getNumber());
        pageableMeta.put("size", pageResult.getSize());
        pageableMeta.put("totalElements", pageResult.getTotalElements());
        pageableMeta.put("totalPages", pageResult.getTotalPages());
        pageableMeta.put("last", pageResult.isLast());
        pageableMeta.put("first", pageResult.isFirst());
        pageableMeta.put("numberOfElements", pageResult.getNumberOfElements());
        pageableMeta.put("sort", pageResult.getSort());
        resp.put("pageable", pageableMeta);

        // 200 + content (even empty) → FE handles empty state
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(HttpSession session, @PathVariable Long id) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");
        if (!userService.isSellerById(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE", "User does not have seller permissions.");

        SellerOrderResponse dto = orderService.createSellerOrderResponseByIdAndUserId(id, userId);
        if (dto == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Order not found or not accessible");

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShipmentStatus(
            HttpSession session,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");
        if (!userService.isSellerById(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE", "User does not have seller permissions.");
        if (!userService.isVerifiedSellerById(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "UNVERIFIED_SELLER", "Your account has not been verified for selling.");

        Order order = orderService.getOrderById(id);
        if (order == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Order not found.");
        if (order.getSeller() == null || !Objects.equals(order.getSeller().getUser_id(), userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ALLOWED", "You are not the seller of this order.");

        String newStatus = body != null ? body.get("newStatus") : null;
        if (newStatus == null || newStatus.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_FIELD", "Missing newStatus");

        ShipmentTrackingStatus stEnum;
        try {
            stEnum = ShipmentTrackingStatus.valueOf(newStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Invalid shipment status. Allowed: " + Arrays.toString(ShipmentTrackingStatus.values()));
        }

        Boolean ok = orderService.updateStatus(order, stEnum);
        if (ok == null || !ok) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_FLOW",
                    "Cannot change shipment status to " + stEnum);
        }

        ShipmentTrackingResponse trackingResponse =
                shipmentTrackingService.createShipmentTrackingResponse(order.getShipmentTracking());
        return ResponseEntity.ok(trackingResponse);
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<?> decideOrder(
            HttpSession session,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");
        if (!userService.isSellerById(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_ROLE", "User does not have seller permissions.");
        if (!userService.isVerifiedSellerById(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "UNVERIFIED_SELLER",
                    "Your account has not been verified for selling.");

        String decision = body != null ? body.get("decision") : null;
        if (decision == null || decision.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_FIELD", "Missing decision");

        Order order = orderService.getOrderById(id);
        if (order == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Order not found.");
        if (order.getSeller() == null || !Objects.equals(order.getSeller().getUser_id(), userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ALLOWED", "You are not the seller of this order.");

        boolean ok = orderService.applySellerDecision(order, decision.toUpperCase(), userId);
        if (!ok)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OPERATION", "Cannot apply decision");

        SellerOrderResponse dto = orderService.createSellerOrderResponseByIdAndUserId(id, userId);
        return ResponseEntity.ok(dto);
    }
}

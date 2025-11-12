package tu_store.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.CartDto;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.models.*;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.UserService;
import tu_store.demo.services.CartItemService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @PostMapping("/draft")
    public ResponseEntity<?> createDraft(HttpSession session, @RequestBody(required = false) DraftCreateRequest body) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null) {
            return ResponseEntity.status(401).body(new ApiSimpleError("UNAUTHORIZED", "Please login first."));
        }

        Cart cart = cartService.getOrCreateCart(userService.getUserBySession(session));
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiSimpleError("EMPTY_CART", "ตะกร้าว่าง"));
        }

        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (p == null) {
                return ResponseEntity.status(404).body(new ApiSimpleError("PRODUCT_NOT_FOUND", "สินค้านี้ไม่มีอยู่หรือไม่สามารถสั่งซื้อได้"));
            }
            if (p.getStatus() == null || p.getStatus() != ProductStatus.AVAILABLE) {
                return ResponseEntity.status(404).body(new ApiSimpleError("PRODUCT_NOT_AVAILABLE", "สินค้านี้ไม่พร้อมให้สั่งซื้อ"));
            }
            if (ci.getQuantity() > p.getStock()) {
                return ResponseEntity.status(409).body(new ApiConflictError("CART_STOCK_EXCEEDED", "จำนวนเกินสต๊อก", p.getStock()));
            }
        }

        Order order = orderService.createOrder(cart);

        OrderDraftResponse resp = new OrderDraftResponse();
        resp.setOrderId(order.getOrderId());
        resp.setStatus("DRAFT");

        List<CartItemDto> items = new ArrayList<>();
        int totalQuantity = 0;
        double totalAmount = 0.0;
        for (CartItem ci : cart.getItems()) {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setProductId(ci.getProductId());
            itemDto.setQuantity(ci.getQuantity());
            itemDto.setPrice(ci.getProduct().getPrice());
            items.add(itemDto);

            totalQuantity += ci.getQuantity();
            totalAmount += ci.getQuantity() * ci.getProduct().getPrice();
        }

        resp.setItems(items);
        resp.setTotalItems(items.size());
        resp.setTotalQuantity(totalQuantity);
        resp.setTotalAmount(totalAmount);

        return ResponseEntity.status(201).body(resp);
    }

    static class ApiSimpleError {
        private String errorCode;
        private String message;
        public ApiSimpleError(String errorCode, String message) {
            this.errorCode = errorCode; this.message = message;
        }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }

    static class ApiConflictError extends ApiSimpleError {
        private Object details;
        public ApiConflictError(String code, String message, Object details) {
            super(code, message);
            this.details = details;
        }
        public Object getDetails() { return details; }
    }

    // Accept optional note or metadata for draft
    public static class DraftCreateRequest {
        private String note;
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}

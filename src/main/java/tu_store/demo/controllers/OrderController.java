package tu_store.demo.controllers;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.UserService;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired private UserService userService;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;


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
}

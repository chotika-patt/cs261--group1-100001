package tu_store.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tu_store.demo.dto.AddToCartRequest;
import tu_store.demo.dto.CartDto;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Product;
import tu_store.demo.services.CartService;
import tu_store.demo.services.ProductService;
import tu_store.demo.services.UserService;

@RestController
@RequestMapping("/api/cart")
public class AddToCartController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    // ======================== ADD TO CART ========================

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(
            HttpSession session,
            @RequestBody AddToCartRequest req
    ){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        if(req == null || req.getProductId() == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "productId is required");

        if(req.getQuantity() <= 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "quantity must be > 0");

        Product p = productService.getProductEntityById(req.getProductId());
        if(p == null)
            throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found");

        if(p.getStatus() == null || p.getStatus() != tu_store.demo.models.ProductStatus.AVAILABLE)
            throw new ApiException(HttpStatus.CONFLICT, "PRODUCT_NOT_AVAILABLE", "สินค้านี้ไม่พร้อมให้สั่งซื้อ");

        if(p.getStock() < req.getQuantity())
            throw new ApiException(HttpStatus.CONFLICT, "CART_STOCK_EXCEEDED", "จำนวนเกินสต๊อก", p.getStock());

        // ⭐ ส่ง size เข้า cartService
        cartService.addItemByUserId(userId, req);

        CartDto response = cartService.createCartResponseByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // ======================== GET CART ========================

    @GetMapping("")
    public ResponseEntity<CartDto> getCart(HttpSession session){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        CartDto dto = cartService.createCartResponseByUserId(userId);
        return ResponseEntity.ok(dto);
    }

    // ======================== SET QUANTITY ========================

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> setItemQuantity(
            HttpSession session,
            @PathVariable Long productId,
            @RequestBody AddToCartRequest req
    ){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        if(req == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "body is required");

        if(req.getQuantity() < 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "quantity must be >= 0");

        cartService.setItemQuantityByUserId(userId, productId, req.getQuantity());

        return ResponseEntity.ok(
                cartService.createCartResponseByUserId(userId)
        );
    }

    // ======================== REMOVE ITEM ========================

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(
            HttpSession session,
            @PathVariable Long productId
    ){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        cartService.removeItemByUserId(userId, productId);

        return ResponseEntity.ok(
                cartService.createCartResponseByUserId(userId)
        );
    }
}

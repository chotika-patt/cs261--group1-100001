package tu_store.demo.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.*;
import tu_store.demo.repositories.UserRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class AddToCartController {
    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    // @GetMapping("/add")
    // public String add(@RequestParam String items){
    //     return "Item Name : " + items;
    // }

    @PostMapping("/add")
    public ResponseEntity<?> add(HttpSession session, @RequestBody CartItemDto item){
    Long userId = userService.getUserIdBySession(session);
    
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");

        cartService.addItemByUserId(userId, item);

        return ResponseEntity.ok(getCart(session));
    }

    @PostMapping("/set")
    public ResponseEntity<?> setQty(HttpSession session, @RequestBody CartItemDto item){
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");

        cartService.setItemQuantityByUserId(userId, item);

        return ResponseEntity.ok(getCart(session));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> remove(HttpSession session, @RequestBody CartItemDto item){
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");

        cartService.removeItemByUserId(userId, item);

        return ResponseEntity.ok(getCart(session));
    }

    @GetMapping("/getCart")
    public ResponseEntity<?> getCart(HttpSession session) {
        Long userId = userService.getUserIdBySession(session);
        
        if (userId == null) return ResponseEntity.status(401).body("Please login first.");

        CartDto response = cartService.createCartResponseByUserId(userId);

        return ResponseEntity.ok(response);
    }
    
}

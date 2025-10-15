package tu_store.demo.controllers;

import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.*;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.CartService;
import tu_store.demo.services.UserService;

import tu_store.demo.dto.CartItemDto;

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
        User user = userService.getUserBySession(session);
        
        if(user == null) return ResponseEntity.status(401).body("Null User!");

        Cart cart = cartService.createCart(user);
        cartService.addItemToCart(cart, item);

        return ResponseEntity.ok("Added!");
    }

    @PostMapping("/setQty")
    public ResponseEntity<?> setQty(HttpSession session, @RequestBody CartItemDto item){
        User user = userService.getUserBySession(session);
        
        if(user == null) return ResponseEntity.status(401).body("Null User!");

        Cart cart = cartService.createCart(user);
        cartService.updateItemQuantity(cart, item);

        return ResponseEntity.ok("Setted!");
    }

    @PostMapping("/remove")
    public ResponseEntity<?> remove(HttpSession session, @RequestBody CartItemDto item){
        User user = userService.getUserBySession(session);
        
        if(user == null) return ResponseEntity.status(401).body("Null User!");

        Cart cart = cartService.createCart(user);
        cartService.removeItemFromCart(cart, item);

        return ResponseEntity.ok("Removed!");
    }
}

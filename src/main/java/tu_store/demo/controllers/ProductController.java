package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.dto.AddToCartRequest;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.dto.ProductSearchRequest;
import tu_store.demo.models.Cart;
import tu_store.demo.models.Product;
import tu_store.demo.models.User;
import tu_store.demo.repositories.CartRepository;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.ProductService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/search")
    public List<Product> searchProducts(@RequestBody ProductSearchRequest searchRequest){
        return productService.search(searchRequest.getName(), searchRequest.getCategory(), searchRequest.getStatus(), searchRequest.getMinPrice(), searchRequest.getMaxPrice());
    }
    @PostMapping("/add")
    public ResponseEntity<?> addProductToDatabase(HttpSession session,@RequestBody Product product) {
        String username = (String) session.getAttribute("username");
        if(username == null){
            return ResponseEntity.status(401).body("Please login as seller.");
        }

        try{
            ProductResponse saved = productService.addProductDTO(product, username);
            return ResponseEntity.ok(saved);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
    @PostMapping("/cart/add")
    public ResponseEntity<?>  addToCart(@RequestBody AddToCartRequest request,HttpSession session) {
        return ResponseEntity.ok("add item to cart successful");
    }
    @PostMapping("/cart/delete")
    public ResponseEntity<?> deleteFromCart(@RequestBody Cart cart) {
        return  ResponseEntity.ok("delete item from cart successful");
    }
    
    

}
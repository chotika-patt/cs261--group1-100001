package tu_store.demo.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tu_store.demo.models.Product;

@RestController
@RequestMapping("/api")
public class ProductController {
    
    @PostMapping("/")
    public void addToCart(@RequestBody Product product  ){
        
    }
}
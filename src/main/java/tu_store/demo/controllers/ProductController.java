package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.dto.ProductSearchRequest;
import tu_store.demo.models.Product;
import tu_store.demo.services.ProductService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestBody ProductSearchRequest searchRequest){
        var results = productService.search(
                searchRequest.getName(),
                searchRequest.getCategory(),
                searchRequest.getStatus(),
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice()
        );


    if (results.isEmpty()){
        return ResponseEntity.ok(Map.of("message", "Nothing match your search terms, please try again."));
    }

    return ResponseEntity.ok(results);

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

}
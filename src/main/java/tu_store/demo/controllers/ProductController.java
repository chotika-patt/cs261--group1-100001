package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tu_store.demo.dto.ProductSearchRequest;
import tu_store.demo.models.Product;
import tu_store.demo.services.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    
    @PostMapping("/")
    public void addToCart(@RequestBody Product product  ){
        
    }

    @Autowired
    private ProductService productService;

    @PostMapping("/search")
    public List<Product> searchProducts(@RequestBody ProductSearchRequest searchRequest){
        return productService.search(searchRequest.getName(), searchRequest.getCategory(), searchRequest.getStatus(), searchRequest.getMinPrice(), searchRequest.getMaxPrice());
    }
}
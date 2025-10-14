package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.repositories.CategoryRepository;
import tu_store.demo.services.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName) {

        List<Product> products;

        if (name != null && !name.isEmpty()) {
            products = productService.findByNameContainingIgnoreCase(name);
        } else if (categoryId != null) {
            Category category = new Category();
            category.setId(categoryId);
            products = productService.findByCategory(category);
        } else if (categoryName != null && !categoryName.isEmpty()) {
            Category category = categoryRepository.findByName(categoryName);
            products = (category != null)
                    ? productService.findByCategory(category)
                    : List.of();
        } else {
            products = productService.findAll();
        }

        return ResponseEntity.ok(products);
    }
}

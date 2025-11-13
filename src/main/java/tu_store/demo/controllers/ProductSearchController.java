package tu_store.demo.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tu_store.demo.services.ProductService;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    @Autowired
    private ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="category", required=false) String category,
            @RequestParam(value="minPrice", required=false) Double minPrice,
            @RequestParam(value="maxPrice", required=false) Double maxPrice,
            @RequestParam(value="inStock", required=false, defaultValue="false") Boolean inStock,
            @RequestParam(value="ratingGte", required=false) Double ratingGte,
            @RequestParam(value="sort", required=false, defaultValue="relevance") String sort,
            @RequestParam(value="page", required=false, defaultValue="1") Integer page,
            @RequestParam(value="pageSize", required=false, defaultValue="20") Integer pageSize
    ) {
        try {
            // validate + normalize inside service
            var result = productService.searchProducts(q, category, minPrice, maxPrice, inStock, ratingGte, sort, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("errorCode", "INVALID_PARAM", "message", ex.getMessage()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorCode","CATEGORY_NOT_FOUND","message",ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorCode","INTERNAL_ERROR","message","Query failed"));
        }
    }
}


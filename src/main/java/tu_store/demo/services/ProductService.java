package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.ProductStatus;
import tu_store.demo.repositories.ProductRepository;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> search(String name, Category category, ProductStatus status, Long minPrice, Long maxPrice) {
        return productRepository.searchProducts(name, category, status, minPrice, maxPrice);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findByNameContainingIgnoreCase(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategory(category);
    }
}

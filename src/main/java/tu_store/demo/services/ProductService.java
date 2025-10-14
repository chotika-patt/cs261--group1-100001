package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.dto.ProductResponse;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.ProductStatus;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    public Product addProduct(Product product,String username){
        User seller = userRepository.findFirstByUsername(username);
        if(seller == null || seller.getRole() != UserRole.SELLER){
            throw new IllegalArgumentException("Only sellers can add products");
        }
        product.setSeller(seller);
        if(product.getStock() > 0){
            product.setStock(product.getStock());
        }else{
            product.setStock(0);
        }
        return productRepository.save(product);
    }
    public List<Product> search(String name, Category category, ProductStatus status, long minPrice, long maxPrice) {
        return productRepository.searchProducts(name, category, status, minPrice, maxPrice);
    }
    public ProductResponse addProductDTO(Product product, String username){
        Product saved = addProduct(product, username);  // ใช้ method เดิม
        return new ProductResponse(
            saved.getProduct_id(),
            saved.getName(),
            saved.getPrice(),
            saved.getStock(),
            saved.getCategory(),
            saved.getStatus(),
            saved.getSeller().getUsername()
        );
    }

}

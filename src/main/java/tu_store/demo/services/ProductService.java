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

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    public Product addProduct(Product product, String username){
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
    public List<Product> search(String name, Category category, ProductStatus status, Long minPrice, Long maxPrice) {
        return productRepository.searchProducts(name, category, status, minPrice, maxPrice);
    }


    public ProductResponse getProductResponseById(Long id){
        Product product = productRepository.findFirstByProductId(id);

        if(product == null) return null;

        ProductResponse response = createProductResponse(product);

        return response;
    } 

    public List<ProductResponse> getAllProductsResponseByUserId(Long id){
        List<Product> products = productRepository.findAllBySellerUserId(id);
        List<ProductResponse> responseList = new ArrayList<>();

        for(Product p : products){
            ProductResponse response = createProductResponse(p);
            responseList.add(response);
        }

        return responseList;
    } 
    public ProductResponse createProductResponse(Product product){
        ProductResponse response = new ProductResponse(
            product.getProductId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            product.getStatus(),
            product.getSeller().getUsername()
        );

        return response;
    }

    public ProductResponse addProductDTO(Product product, String username){
        Product saved = addProduct(product, username);  // ใช้ method เดิม
        return createProductResponse(saved);
    }
    
}

package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.models.*;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.ReviewRepository;
import tu_store.demo.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    // =============== Add Product ===============
    public Product addProduct(Product product, String username){
        User seller = userRepository.findFirstByUsername(username);

        if (seller == null || seller.getRole() != UserRole.SELLER) {
            throw new IllegalArgumentException("Only sellers can add products");
        }

        product.setSeller(seller);

        if (product.getStock() <= 0) {
            product.setStock(0);
        }

        return productRepository.save(product);
    }

    // =============== Search Products (NEW) ===============
    public List<Product> search(
            String name,
            Category category,
            Long minPrice,
            Long maxPrice,
            Double rating,
            Boolean inStock,
            String sort
    ) {
        // 1) ดึงข้อมูลด้วย filter (ใช้ index)
        List<Product> products = productRepository.searchProducts(
                name,
                category,
                minPrice,
                maxPrice,
                rating,
                inStock
        );

        // 2) Sort ใน service เพื่อรองรับหลายแบบ
        if (sort != null) {
            switch (sort) {
                case "price_asc" -> products.sort(Comparator.comparing(Product::getPrice));
                case "price_desc" -> products.sort(Comparator.comparing(Product::getPrice).reversed());
                case "best_selling" -> products.sort(Comparator.comparing(Product::getSoldCount).reversed());
                case "latest" -> products.sort(Comparator.comparing(Product::getUpdatedAt).reversed());
                case "newest" -> products.sort(Comparator.comparing(Product::getCreatedAt).reversed());
                case "rating" -> products.sort(Comparator.comparing(Product::getRatingAvg).reversed());
                default -> {}
            }
        }

        return products;
    }


    // =============== Product Response ===============
    public ProductResponse getProductResponseById(Long id){
        Product product = productRepository.findFirstByProductId(id);
        if(product == null) return null;

        return createProductResponse(product);
    }

    public List<ProductResponse> getAllProductsResponseByUserId(Long id){
        List<Product> products = productRepository.findAllBySellerUserId(id);
        List<ProductResponse> responseList = new ArrayList<>();

        for(Product p : products){
            responseList.add(createProductResponse(p));
        }
        return responseList;
    }

    public ProductResponse createProductResponse(Product product){
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getStatus(),
                product.getSeller().getUsername()
        );
    }

    public ProductResponse addProductDTO(Product product, String username){
        Product saved = addProduct(product, username);
        return createProductResponse(saved);
    }


    // =============== Rating Update ===============
    @Transactional
    public void updateProductRatingById(Long productId) {
        List<Review> reviews = reviewRepository.findAllByProductProductId(productId);

        int count = reviews.size();
        double avg = 0;

        if (count > 0) {
            avg = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .sum() / count;

            avg = Math.round(avg * 100.0) / 100.0;
        }

        Product product = productRepository.findFirstByProductId(productId);
        if(product == null) return;

        product.setRatingCount(count);
        product.setRatingAvg(avg);

        productRepository.save(product);
    }

    public Product getProductEntityById(Long id) {
        if (id == null) return null;
        return productRepository.findFirstByProductId(id);
    }
}

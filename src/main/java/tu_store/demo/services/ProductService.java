package tu_store.demo.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.models.CartItem;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.ProductStatus;
import tu_store.demo.models.Review;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.ReviewRepository;
import tu_store.demo.repositories.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

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

    /*public List<Product> search(String name, Category category, ProductStatus status, Long minPrice, Long maxPrice) {
        return productRepository.searchProducts(name, category, status, minPrice, maxPrice);
    }*/


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

    public ProductResponse addProductDTO(Product product, String username){
        Product saved = addProduct(product, username);  // ใช้ method เดิม
        return createProductResponse(saved);
    }

    @Transactional
    public void updateProductRatingById(Long productId) {
        List<Review> reviews = reviewRepository.findAllByProductProductId(productId);

        int count = reviews.size();
        double avg = 0;

        if (count > 0) {
            avg = reviews.stream()
                .mapToDouble(Review::getRating)
                .sum();

            avg = avg / count;
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

    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(
            String q,
            String category,
            Double minPrice,
            Double maxPrice,
            Boolean inStock,
            Double ratingGte,
            String sort,
            Integer page,
            Integer pageSize
    ) {
        // validation
        if (page == null || page < 1) throw new IllegalArgumentException("page must be >= 1");
        if (pageSize == null || pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
        if (pageSize > 100) pageSize = 100;
        if (minPrice != null && minPrice < 0) throw new IllegalArgumentException("minPrice must be >= 0");
        if (maxPrice != null && maxPrice < 0) throw new IllegalArgumentException("maxPrice must be >= 0");
        if (minPrice != null && maxPrice != null && maxPrice < minPrice)
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        if (ratingGte != null && (ratingGte < 0.0 || ratingGte > 5.0))
            throw new IllegalArgumentException("ratingGte must be between 0 and 5");

        if (category != null && !category.isBlank()) {
            if (!categoryExists(category)) {
                throw new EntityNotFoundException("Category '" + category + "' not found");
            }
        }

        // normalize q
        String normalizedQ = normalizeQuery(q);

        // sort & pageable
        Sort sortSpec = buildSortSpec(sort, normalizedQ);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sortSpec);

        // specification (inline)
        var spec = buildSearchSpec(normalizedQ, category, minPrice, maxPrice, inStock, ratingGte);

        Page<Product> pageRes = productRepository.findAll(spec, pageable);

        List<ProductResponse> items = pageRes.stream()
                .map(this::createProductResponse)
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("total", pageRes.getTotalElements());
        resp.put("page", page);
        resp.put("pageSize", pageSize);
        return resp;
    }

    // --- normalize helper ---
    private String normalizeQuery(String q) {
        if (q == null) return null;
        String s = q.trim().replaceAll("\\s+", " ").toLowerCase();
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    // --- sort helper ---
    private Sort buildSortSpec(String sort, String normalizedQ) {
        if (sort == null || sort.isBlank()) sort = "relevance";
        switch (sort) {
            case "price_asc": return Sort.by("price").ascending();
            case "price_desc": return Sort.by("price").descending();
            case "rating_asc": return Sort.by("ratingAvg").ascending();
            case "rating_desc": return Sort.by("ratingAvg").descending();
            case "created_asc": return Sort.by("createdAt").ascending();
            case "created_desc": return Sort.by("createdAt").descending();
            case "relevance":
            default:
                if (normalizedQ == null || normalizedQ.isBlank()) {
                    return Sort.by("createdAt").descending();
                }
                return Sort.unsorted();
        }
    }


    private Category parseCategory(String category) {
        if (category == null || category.isBlank()) return null;
        String normalized = Normalizer.normalize(category.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\s-]+", "_")   // spaces/hyphens → underscore
                .replaceAll("[^A-Z0-9_]", "") // remove other chars after uppercasing
                .toUpperCase();
        try {
            return Category.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private org.springframework.data.jpa.domain.Specification<Product> buildSearchSpec(
            String q,
            String category,
            Double minPrice,
            Double maxPrice,
            Boolean inStock,
            Double ratingGte
    ) {
        final Category catEnum = parseCategory(category); // parse once

        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                // q is likely already normalized/lowercased, but make sure pattern is lower-case for comparison
                String like = "%" + q.toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            // compare enum directly (safe)
            if (catEnum != null) {
                preds.add(cb.equal(root.get("category"), catEnum));
            }

            if (minPrice != null) preds.add(cb.ge(root.get("price"), minPrice));
            if (maxPrice != null) preds.add(cb.le(root.get("price"), maxPrice));
            if (Boolean.TRUE.equals(inStock)) preds.add(cb.gt(root.get("stock"), 0));
            if (ratingGte != null) preds.add(cb.ge(root.get("ratingAvg"), ratingGte));

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private ProductResponse createProductResponse(Product p) {
        if (p == null) return null;
        String sellerName = null;
        try {
            if (p.getSeller() != null) sellerName = p.getSeller().getUsername();
        } catch (Exception ignored) {
            sellerName = null;
        }
        return new ProductResponse(
                p.getProductId(),
                p.getName(),
                p.getPrice(),
                p.getStock(),
                p.getCategory(),
                p.getStatus(),
                sellerName
        );
    }

    private boolean categoryExists(String category) {
        if (category == null || category.isBlank()) return false;

        // Normalize: trim, remove diacritics, replace spaces/hyphens with underscore, uppercase
        String normalized = category.trim().toLowerCase();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[\\s-]+", "_"); // space or hyphen -> underscore
        normalized = normalized.replaceAll("[^a-z0-9_]", ""); // remove other chars
        normalized = normalized.toUpperCase();

        try {
            Category.valueOf(normalized);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

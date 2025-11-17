package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.dto.CartItemDto;
import tu_store.demo.models.*;
import tu_store.demo.repositories.*;

@Service
public class CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // ========================= CREATE ITEM =========================

    public CartItem createItem(Cart cart, Product product, int qty, String size) {

        if (qty <= 0) return null;

        // สินค้าไม่มี size → บังคับให้ size = null
        if (size != null && size.trim().isEmpty()) size = null;

        return new CartItem(cart, product, qty, size);
    }

    public CartItem createItem(Cart cart, CartItemDto dto) {
        if (dto == null) return null;

        Long productId = dto.getProductId();
        if (productId == null) return null;

        Product product = productRepository.findFirstByProductId(productId);
        if (product == null) return null;

        int qty = dto.getQuantity();
        if (qty <= 0) return null;

        String size = dto.getSize();
        if (size != null && size.trim().isEmpty()) size = null;

        return new CartItem(cart, product, qty, size);
    }

    // ========================= Convert to DTO =========================

    public CartItemDto createCartItemResponse(CartItem item){
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct().getPrice());

        // ⭐ NEW
        dto.setSize(item.getSize());

        return dto;
    }

    public double calculateTotalPrice(CartItem item){
        return item.getQuantity() * item.getProduct().getPrice();
    }

    public boolean isStockAvailable(CartItem item, int qty){
        if (item == null) return false;
        return item.getProduct().getStock() >= qty;
    }

}

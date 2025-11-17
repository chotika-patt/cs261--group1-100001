package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.dto.CartItemDto;
import tu_store.demo.models.Cart;
import tu_store.demo.models.CartItem;
import tu_store.demo.models.Product;
import tu_store.demo.repositories.ProductRepository;

@Service
public class CartItemService {

    @Autowired
    private ProductRepository productRepository;

    public CartItem createItem(Cart cart, Product product, int qty, String size) {
        if (qty <= 0) return null;
        if (size != null && size.trim().isEmpty()) size = null;

        return new CartItem(cart, product, qty, size);
    }

    public CartItemDto createCartItemResponse(CartItem item){
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct().getPrice());
        dto.setSize(item.getSize());

        // ⭐ เพิ่ม name และรูป
        dto.setProductName(item.getProduct().getName());
        dto.setProductImage(item.getProduct().getMain_image());

        return dto;
    }

    public double calculateTotalPrice(CartItem item){
        return item.getQuantity() * item.getProduct().getPrice();
    }
}

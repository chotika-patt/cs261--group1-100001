package tu_store.demo.dto;

import java.util.List;

public class CartDto {
    private Long userId;
    private Long cartId;
    private List<CartItemDto> items;

    private double subtotalPrice;
    private double totalPrice;

    public CartDto(){}

    public void setSubtotalPrice(double subtotalPrice) {
        this.subtotalPrice = subtotalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public Long getCartId() {
        return cartId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public double getSubtotalPrice() {
        return subtotalPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}

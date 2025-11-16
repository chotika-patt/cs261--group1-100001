package tu_store.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItem_id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    // ⭐ NEW: size ของสินค้า (nullable)
    @Column(name = "size", columnDefinition = "NVARCHAR(10)", nullable = true)
    private String size;

    public CartItem() {}

    public CartItem(Cart cart, Product product, int quantity, String size) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.size = size;
    }

    public Long getCartItem_id() {
        return cartItem_id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public long getProductId() {
        return this.product.getProductId();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int qty) {
        if (qty < 0) return;
        this.quantity = qty;
    }

    // ⭐ size getter/setter
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}

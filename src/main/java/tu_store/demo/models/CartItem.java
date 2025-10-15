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

    public CartItem(){}

    public CartItem(Cart cart, Product product, int quantity){
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public void setCart(Cart cart){
        this.cart = cart;
    }

    public Cart getCart(){
        return this.cart;
    }

    public void setQuantity(int qty){
        this.quantity = qty;
    }

    public int getQuantity(){
        return this.quantity;
    }

    public long getProductId(){
        return this.product.getProduct_id();
    }

    public Product getProduct(){
        return this.product;
    }
}

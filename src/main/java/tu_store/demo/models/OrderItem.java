package tu_store.demo.models;

import jakarta.persistence.*;


@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    public OrderItem() {}

    public OrderItem(Order order, CartItem cartItem, double price){
        this.order = order;
        this.product = cartItem.getProduct();
        this.quantity = cartItem.getQuantity();
        this.totalPrice = price;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Long getOrderId() {
        return order.getOrderId();
    }

    public Long getProductId() {
        return product.getProductId();
    }

    public int getQuantity() {
        return quantity;
    }
}

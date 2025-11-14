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

    // จำนวนที่ซื้อ
    private int quantity;

    // ราคาต่อชิ้นตอนซื้อ (สำคัญมาก)
    @Column(name = "price", nullable = true)
    private long price;

    // ราคารวม = price × quantity
    @Column(name = "total_price", nullable = true)
    private double totalPrice;

    public OrderItem() {}

    // Constructor ใช้สร้างจาก CartItem
    public OrderItem(Order order, CartItem cartItem) {
        this.order = order;
        this.product = cartItem.getProduct();
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getProduct().getPrice(); // ราคา ณ ตอนซื้อ
        this.totalPrice = this.price * this.quantity;
    }

    // ===== Getter & Setter =====
    public Long getOrderItemId() {
        return orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getOrderId() {
        return order.getOrderId();
    }

    public Long getProductId() {
        return product.getProductId();
    }
}

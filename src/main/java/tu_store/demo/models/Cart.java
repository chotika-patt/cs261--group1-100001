package tu_store.demo.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.*;

@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id")
    private String sessionId;  
 

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> items = new ArrayList<>();
    
    public Cart() {}
    
    
    public Cart(String id){
        this.sessionId = id;
    } 
    
    public Cart(User user){
        this.user = user;
    }
    public void setUser(User user){
        this.user = user;
    }

    public void addItem(CartItem item){
        items.add(item);
    }

    public CartItem findFirstItemById(long id){
        return this.getItems().stream()
        .filter(item -> item.getProductId() == id)
        .findFirst()
        .orElse(null);
    }

    public CartItem findFirstItemByProduct(Product product){
        return this.getItems().stream()
        .filter(item -> item.getProductId() == product.getProduct_id())
        .findFirst()
        .orElse(null);
    }
    public List<CartItem> getItems(){
        return items;
    }

    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }
}
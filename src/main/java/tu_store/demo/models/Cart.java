package tu_store.demo.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.*;
import tu_store.demo.dto.CartItemDto;

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
    
    // getters & setters
    public Cart() {}
    

    public Cart(String id){
        this.sessionId = id;
    }

    public Cart(User user){
        this.user = user;
    }

    public void setSessionId(String id){
        this.sessionId = id;
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

    // public List<CartItemDto> getItemDtos(){
    //     List<CartItemDto> itemDtos = new ArrayList<>();

    //     for (CartItem item : this.items) {
    //         CartItemDto dto = new CartItemDto();
    //         dto.setProductId(item.getProductId());
    //         dto.setQuantity(item.getQuantity());
    //         itemDtos.add(dto);
    //     }

    //     return itemDtos;
    // }

    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }

    public Long getCartId() {
        return cartId;
    }
}
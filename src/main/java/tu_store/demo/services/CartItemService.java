package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.dto.CartItemDto;
import tu_store.demo.models.*;
import tu_store.demo.repositories.*;
// import tu_store.demo.services.*;

@Service
public class CartItemService {


    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;


    public CartItem createItem(Cart cart, Product product, int qty) {
        return new CartItem(cart, product, qty);
    }

    public CartItem createItem(Cart cart, CartItemDto dto) {
        Product product = productRepository.findFirstByProductId(dto.getProductId());
        
        if(product == null) return null;

        return new CartItem(cart, product, dto.getQuantity());
    }

    public double calculateTotalPrice(CartItem item){
        if(item == null) return 0;
        return item.getQuantity() * item.getProduct().getPrice();
    }

    public boolean isStockAvailable(CartItem item, int qty){
        if(item == null) return false;

        Product product = item.getProduct();
        if (product.getStock() >= qty){
            return true;
        }
        else return false;
    }

    public void removeItem(Cart cart, CartItem oldItem) {
        if(cart == null || oldItem == null) return;
  
        CartItem item = cart.getItems().stream()
        .filter(itemI -> itemI.getProductId() == oldItem.getProductId()).findFirst().orElse(null);
        
        if(item == null) return;

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
    }

    public void setQuantity(CartItem item, int qty){
        if(item == null) return;
        if(!isStockAvailable(item, qty)) return;

        item.setQuantity(qty);
        cartItemRepository.save(item);
    }

    public void changeQuantityBy(CartItem item, int qty){
        if(item == null) return;
        if(qty + item.getQuantity() <= 0){
            removeItem(item.getCart(), item);
        }
        else{
            setQuantity(item, item.getQuantity() + qty);
        }
    }
}

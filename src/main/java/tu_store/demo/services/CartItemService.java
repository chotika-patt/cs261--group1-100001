package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.models.*;
import tu_store.demo.repositories.*;
// import tu_store.demo.services.*;

@Service
public class CartItemService {


    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;



    public CartItem createItem(Cart cart, Product product, int qty) {
        CartItem item = new CartItem(cart, product, qty);

        return item;
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

    public void removeItem(Cart cart, CartItem item) {
        if(cart == null) return;

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

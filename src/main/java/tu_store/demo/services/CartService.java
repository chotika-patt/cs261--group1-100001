package tu_store.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.models.*;
import tu_store.demo.repositories.*;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemService cartItemService;

    // @Autowired
    // private CartItemRepository cartItemRepository;



    public Cart createCart(User user){
        Cart cart = cartRepository.findFirstByUserUserId(user.getUser_id());

        if(cart == null){
            cart = new Cart(user);
            cartRepository.save(cart);
        }
        return cart;
    }

    public Cart createCart(String sessionId){
        Cart cart = cartRepository.findFirstBySessionId(sessionId);

        if(cart == null){
            cart = new Cart(sessionId);
            cartRepository.save(cart);
        }
        return cart;
    }

    public void addItemToCart(Cart cart, CartItem newItem) {
        CartItem oldItem = cart.getItems().stream()
        .filter(item -> item.getProductId() == newItem.getProductId()).findFirst().orElse(null);

        if(oldItem != null){
            cartItemService.changeQuantityBy(oldItem, newItem.getQuantity());
        }
        else{
            newItem.setCart(cart);
            cart.addItem(newItem);
        }
        cartRepository.save(cart);
    }

    public void addItemsToCard(Cart cart, List<CartItem> items){
        for (CartItem item : items) {
            addItemToCart(cart, item);
        }
    }

    public void addProductToCart(Cart cart, Product product) {
        CartItem newItem = cartItemService.createItem(cart, product, 1);

        addItemToCart(cart, newItem);
    }
    public void addProductToCart(Cart cart, Product product, int qty) {
        CartItem newItem = cartItemService.createItem(cart, product, qty);

        addItemToCart(cart, newItem);
    }
    public void addProductsToCart(Cart cart, List<Product> products){
        for (Product product : products) {
            CartItem item = cartItemService.createItem(cart, product, 1);

            item.setCart(cart);
            cart.addItem(item); 
        }
        cartRepository.save(cart);
    }

    public void removeItemFromCart(Cart cart, CartItem item){
        if(item == null) return;
        if(cart.findFirstItemById(item.getProductId()) == null) return;
        cartItemService.removeItem(cart, item);
    }

    public void changeItemQuantityBy(CartItem item, int qty){
        cartItemService.changeQuantityBy(item, qty);
    }

    public double calculateTotalPrice(Cart cart){
        double price = 0;

        for(CartItem item : cart.getItems()){
            price = price + cartItemService.calculateTotalPrice(item);
        }

        return price;
    }
}

package tu_store.demo.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.CartDto;
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


    public CartDto createCartResponse(Cart cart){
        CartDto response = new CartDto();
        response.setCartId(cart.getCartId());
        response.setUserId(cart.getUserId());

        List<CartItemDto> items = new ArrayList<>();

        for(CartItem item : cart.getItems()){
            CartItemDto itemDto = cartItemService.createCartItemResponse(item);

            items.add(itemDto);
        }

        response.setSubtotalPrice(calculateSubtotalPrice(cart));
        response.setTotalPrice(calculateTotalPrice(cart));
        response.setItems(items);

        return response;
    }
    public CartDto createCartResponseByUserId(Long id){
        Cart cart = cartRepository.findFirstByUserUserId(id);
        if(cart == null) return null;

        return createCartResponse(cart);
    }

    public void addItemToCart(Cart cart, CartItemDto dto){
        addItemToCart(cart, cartItemService.createItem(cart, dto));
    }
    public void addItemToCart(Cart cart, CartItem newItem) {
        if(newItem.getProduct().getStatus() != ProductStatus.AVAILABLE) return;

        CartItem oldItem = cart.getItems().stream()
        .filter(item -> item.getProductId() == newItem.getProductId()).findFirst().orElse(null);

        if(oldItem != null){
            cartItemService.changeQuantityBy(oldItem, newItem.getQuantity());
            return;
        } 
        
        if(newItem.getQuantity() <= 0) return;
        if(!cartItemService.isStockAvailable(newItem, newItem.getQuantity())) return;

        cart.addItem(newItem);
        newItem.setCart(cart);
        cartRepository.save(cart);
    }
    public void addItemsToCart(Cart cart, List<CartItem> items){
        for (CartItem item : items) {
            addItemToCart(cart, item);
        }
    }
    public void addProductToCart(Cart cart, Product product, int qty){
        addItemToCart(cart, cartItemService.createItem(cart, product, qty));
    }
    public void addItemByUserId(Long id, CartItemDto dto){
        Cart cart = cartRepository.findFirstByUserUserId(id);
        if(cart == null) return;

        addItemToCart(cart, dto);
    }

    public void removeItem(Cart cart, CartItemDto dto){
        removeItem(cart, cartItemService.createItem(cart, dto));
    }
    public void removeItem(Cart cart, CartItem item){
        if(item == null) return;
        if(cart.findFirstItemById(item.getProductId()) == null) return;
        cartItemService.removeItem(cart, item);
    }
    public void removeItemByUserId(Long id, CartItemDto dto){
        Cart cart = cartRepository.findFirstByUserUserId(id);

        if(cart == null) return;
        removeItem(cart, dto);
    }

    public void setItemQuantity(Cart cart, CartItemDto dto){
        CartItem newItem = cartItemService.createItem(cart, dto);
        if(newItem.getProduct().getStatus() != ProductStatus.AVAILABLE) return;

        cartItemService.setQuantity(newItem, dto.getQuantity());
    }
    public void setItemQuantityByUserId(Long id, CartItemDto dto){
        Cart cart = cartRepository.findFirstByUserUserId(id);
        if(cart == null) return;

        setItemQuantity(cart, dto);
    }

    public void changeItemQuantityBy(CartItem item, int qty){
        cartItemService.changeQuantityBy(item, qty);
    }
    public void changeItemQuantityBy(Cart cart, CartItemDto dto, int qty){
        cartItemService.changeQuantityBy(cartItemService.createItem(cart, dto), qty);
    }

    public double calculateSubtotalPrice(Cart cart){
        double price = 0;

        for(CartItem item : cart.getItems()){
            price = price + cartItemService.calculateTotalPrice(item);
        }

        return price;
    }
    public double calculateTotalPrice(Cart cart){
        double price = calculateSubtotalPrice(cart);
        double vat = price * 0.07; // Vat 7%;
        price = price + vat;

        return price;
    }
}

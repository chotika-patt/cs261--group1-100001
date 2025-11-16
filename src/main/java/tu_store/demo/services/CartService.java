package tu_store.demo.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tu_store.demo.dto.AddToCartRequest;
import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.CartDto;
import tu_store.demo.models.*;
import tu_store.demo.repositories.CartRepository;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // ===========================================================
    // CART LOADING
    // ===========================================================

    @Transactional
    public Cart getOrCreateCartByUserId(Long userId) {
        if (userId == null) return null;

        Cart cart = cartRepository.findFirstByUserUserIdAndIsActiveTrue(userId);
        if (cart != null) return cart;

        User user = userRepository.findFirstByUserId(userId);
        if (user == null) return null;

        Cart newCart = new Cart(user);
        cartRepository.save(newCart);
        return newCart;
    }

    // ===========================================================
    // ADD ITEM
    // ===========================================================

    public void addItemByUserId(Long userId, AddToCartRequest req) {
        Cart cart = getOrCreateCartByUserId(userId);
        if (cart == null) return;

        Product product = productRepository.findFirstByProductId(req.getProductId());
        if (product == null) return;

        String size = req.getSize();
        if (size != null && size.trim().isEmpty()) size = null;

        CartItem newItem =
                cartItemService.createItem(cart, product, req.getQuantity(), size);

        addItemToCart(cart, newItem);
    }

    public void addItemToCart(Cart cart, CartItem newItem) {
        if (cart == null || newItem == null || newItem.getProduct() == null) return;

        Long newProductId = newItem.getProductId();
        String newSize = newItem.getSize();

        // ⭐ เช็คว่ามี productId เดียวกัน + size เดียวกันหรือไม่
        CartItem oldItem = cart.getItems().stream()
                .filter(item ->
                        item.getProductId() == newProductId &&
                                ((item.getSize() == null && newSize == null) ||
                                 (item.getSize() != null && item.getSize().equals(newSize)))
                )
                .findFirst()
                .orElse(null);

        if (oldItem != null) {
            int updatedQty = oldItem.getQuantity() + newItem.getQuantity();
            oldItem.setQuantity(updatedQty);
            cartRepository.save(cart);
            return;
        }

        // add new item
        cart.addItem(newItem);
        newItem.setCart(cart);
        cartRepository.save(cart);
    }

    // ===========================================================
    // RESPONSE BUILDER
    // ===========================================================

    public CartDto createCartResponseByUserId(Long id){
        Cart cart = getOrCreateCartByUserId(id);
        return createCartResponse(cart);
    }

    public CartDto createCartResponse(Cart cart){
        if (cart == null) return null;

        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());

        try { dto.setUserId(cart.getUserId()); }
        catch (Exception e) { dto.setUserId(null); }

        List<CartItemDto> items = new ArrayList<>();
        for (CartItem item : cart.getItems()){
            items.add(cartItemService.createCartItemResponse(item));
        }

        dto.setItems(items);
        dto.setSubtotalPrice(calculateSubtotalPrice(cart));
        dto.setTotalPrice(calculateTotalPrice(cart));
        return dto;
    }

    // ===========================================================
    // REMOVE ITEM
    // ===========================================================

    public void removeItemByUserId(Long userId, Long productId) {
        if (userId == null || productId == null) return;

        Cart cart = getOrCreateCartByUserId(userId);
        if (cart == null) return;

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProductId() == productId)
                .findFirst()
                .orElse(null);

        if (item != null) {
            cart.getItems().remove(item);
            cartRepository.save(cart);
        }
    }

    // ===========================================================
    // SET ITEM QUANTITY
    // ===========================================================

    public void setItemQuantityByUserId(Long userId, Long productId, int qty) {
        if (userId == null || productId == null) return;

        Cart cart = getOrCreateCartByUserId(userId);
        if (cart == null) return;

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProductId() == productId)
                .findFirst()
                .orElse(null);

        if (item == null) return;

        if (qty <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(qty);
        }

        cartRepository.save(cart);
    }

    // ===========================================================
    // PRICE CALC
    // ===========================================================

    public double calculateSubtotalPrice(Cart cart){
        return cart.getItems().stream()
                .mapToDouble(cartItemService::calculateTotalPrice)
                .sum();
    }

    public double calculateTotalPrice(Cart cart){
        double subtotal = calculateSubtotalPrice(cart);
        return subtotal + (subtotal * 0.07);
    }
}

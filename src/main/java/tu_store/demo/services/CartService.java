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

    @Transactional
    public Cart getOrCreateCart(User user){
        if(user == null) return null;
        Cart cart = cartRepository.findFirstByUserUserIdAndIsActiveTrue(user.getUser_id());

        if(cart == null){
            cart = new Cart(user);
            cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public Cart getOrCreateCart(String sessionId){
        Cart cart = cartRepository.findFirstBySessionIdAndIsActiveTrue(sessionId);

        if(cart == null){
            cart = new Cart(sessionId);
            cartRepository.save(cart);
        }
        return cart;
    }

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

    public List<CartItem> getItemsByUserId(Long userId) {
        Cart cart = cartRepository.findFirstByUserUserIdAndIsActiveTrue(userId);
        if (cart == null) return List.of();
        return cart.getItems();
    }

    // --------------------- Response builders ---------------------

    public CartDto createCartResponse(Cart cart){
        if (cart == null) return null;

        CartDto response = new CartDto();
        response.setCartId(cart.getCartId());

        try {
            response.setUserId(cart.getUserId());
        } catch (Exception e) {
            // cart might be session-based without user
            response.setUserId(null);
        }

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
        Cart cart = getOrCreateCartByUserId(id);
        if(cart == null) return null;

        return createCartResponse(cart);
    }

    // --------------------- Add / Remove / Set ---------------------

    public void addItemToCart(Cart cart, CartItemDto dto){
        if (cart == null || dto == null) return;
        addItemToCart(cart, cartItemService.createItem(cart, dto));
    }

    public void addItemToCart(Cart cart, CartItem newItem) {
        if (newItem == null || newItem.getProduct() == null) return;
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

    public void addProductToCart(Cart cart, Product product, int qty){
        addItemToCart(cart, cartItemService.createItem(cart, product, qty));
    }

    public void addItemByUserId(Long userId, AddToCartRequest req) {
        if (userId == null || req == null) return;

        Cart cart = getOrCreateCartByUserId(userId);
        if (cart == null) return;

        Product product = productRepository.findFirstByProductId(req.getProductId());
        if (product == null) return;

        addProductToCart(cart, product, req.getQuantity());
    }

    // --------------------- Remove ---------------------

    public void removeItem(Cart cart, CartItemDto dto){
        if (cart == null || dto == null) return;
        removeItem(cart, cartItemService.createItem(cart, dto));
    }

    public void removeItem(Cart cart, CartItem item){
        if(item == null || cart == null) return;
        CartItem found = cart.findFirstItemById(item.getProductId());
        if(found == null) return;
        cartItemService.removeItem(cart, found);
    }

    public void removeItemByUserId(Long id, CartItemDto dto){
        Cart cart = cartRepository.findFirstByUserUserIdAndIsActiveTrue(id);
        if(cart == null) return;
        removeItem(cart, dto);
    }

    public void removeItemByUserId(Long id, Long productId){
        if (id == null || productId == null) return;
        Cart cart = cartRepository.findFirstByUserUserIdAndIsActiveTrue(id);
        if (cart == null) return;
        CartItem found = cart.findFirstItemById(productId);
        if (found == null) return;
        cartItemService.removeItem(cart, found);
    }

    // --------------------- Set quantity ---------------------

    public void setItemQuantity(Cart cart, CartItemDto dto){
        if (cart == null || dto == null) return;
        CartItem newItem = cartItemService.createItem(cart, dto);
        if (newItem == null || newItem.getProduct() == null) return;
        if(newItem.getProduct().getStatus() != ProductStatus.AVAILABLE) return;

        cartItemService.setQuantity(newItem, dto.getQuantity());
    }

    public void setItemQuantityByUserId(Long id, CartItemDto dto){
        Cart cart = getOrCreateCartByUserId(id);
        if(cart == null) return;

        setItemQuantity(cart, dto);
    }

    public void setItemQuantityByUserId(Long id, Long productId, int qty){
        if (id == null || productId == null) return;
        Cart cart = getOrCreateCartByUserId(id);
        if (cart == null) return;

        CartItemDto dto = new CartItemDto();
        dto.setProductId(productId);
        dto.setQuantity(qty);
        setItemQuantity(cart, dto);
    }

    // --------------------- Helpers ---------------------

    public double calculateSubtotalPrice(Cart cart){
        if (cart == null) return 0.0;
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

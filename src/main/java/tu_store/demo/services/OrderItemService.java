package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.models.*;
import tu_store.demo.repositories.*;
// import tu_store.demo.services.*;

@Service
public class OrderItemService {

    
    @Autowired
    private CartItemService cartItemService;

    public OrderItem createItem(Order order, CartItem cartItem) {
        return new OrderItem(order, cartItem, cartItemService.calculateTotalPrice(cartItem));
    }
}

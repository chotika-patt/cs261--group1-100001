package tu_store.demo.services;

import org.springframework.stereotype.Service;

import tu_store.demo.models.CartItem;
import tu_store.demo.models.Order;
import tu_store.demo.models.OrderItem;

@Service
public class OrderItemService {

    public OrderItem createItem(Order order, CartItem cartItem) {
        // ใช้ constructor ใหม่ คำนวณ price & totalPrice อัตโนมัติ
        return new OrderItem(order, cartItem);
    }
}

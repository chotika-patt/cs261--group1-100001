package tu_store.demo.services;

import org.springframework.stereotype.Service;

import tu_store.demo.dto.BuyerOrderItemResponse;
import tu_store.demo.dto.SellerOrderItemResponse;
import tu_store.demo.models.CartItem;
import tu_store.demo.models.Order;
import tu_store.demo.models.OrderItem;

@Service
public class OrderItemService {

    public OrderItem createItem(Order order, CartItem cartItem) {
        // ใช้ constructor ใหม่ คำนวณ price & totalPrice อัตโนมัติ
        return new OrderItem(order, cartItem);
    }

    // -----------------------------------------------------
    // CREATE BUYER ORDER RESPONSE FOR CONTROLLER
    // -----------------------------------------------------

    public BuyerOrderItemResponse createClientOrderItemResponse(OrderItem item){
        BuyerOrderItemResponse response = new BuyerOrderItemResponse();
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setTotalPrice(item.getTotalPrice());

        return response;
    }

    // -----------------------------------------------------
    // CREATE SELLER ORDER RESPONSE FOR CONTROLLER
    // -----------------------------------------------------

    public SellerOrderItemResponse createSellerOrderItemResponse(OrderItem item){
        SellerOrderItemResponse response = new SellerOrderItemResponse();
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setTotalPrice(item.getTotalPrice());

        return response;
    }
}

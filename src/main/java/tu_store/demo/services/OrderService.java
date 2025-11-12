package tu_store.demo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tu_store.demo.models.*;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;
import tu_store.demo.repositories.*;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShipmentTrackingService shipmentTrackingService;

    public Order createOrder(Cart cart){
        Order order = orderRepository.findFirstByCartCartId(cart.getCartId());

        if(order == null){
            order = new Order(cart);

            List<OrderItem> items = new ArrayList<>();

            for(CartItem cartItem : cart.getItems()){
                items.add(orderItemService.createItem(order, cartItem));
            }

            order.setItems(items);
            order.setTotalPrice(calculateTotalPrice(order));
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
        }
        return order;
    }


    public List<Order> createOrdersResponseByUserId(Long id){
        return orderRepository.findAllByUserUserId(id);
    }

    public void checkoutByUserId(Long id){
        Cart cart = cartRepository.findFirstByUserUserIdAndIsActiveTrue(id);
    
        if (cart == null || cart.getItems().isEmpty()) {
            return;
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (product.getStock() < item.getQuantity()) {
                return;
            }
        }

        cart.setActive(false);
        cartRepository.save(cart);

        createOrder(cart);
    }

    public Order getOrderById(Long id){
        return orderRepository.findFirstByOrderId(id);
    }

    public Order updateStatus(Order order, OrderStatus status){
        if(order == null || status == null) return null;
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return null;


        if(status == OrderStatus.PAID) {
            order.setStatus(status);

            ShipmentTracking st = shipmentTrackingService.getOrCreateShipmentTracking(order);
            order.setShipmentTracking(st);
        } else if(status == OrderStatus.COMPLETED) {
            if(order.getShipmentTracking() == null || order.getShipmentTracking().getStatus() != ShipmentTrackingStatus.DELIVERED){
                return null;
            }

            order.setStatus(status);
        }

        orderRepository.save(order);
        return order;
    }
    public Order updateStatus(Order order){
        if(order == null) return null;
        if(order.getStatus() == OrderStatus.PENDING){
            return updateStatus(order, OrderStatus.PAID);

        } else if(order.getStatus() == OrderStatus.PAID){
            return updateStatus(order, OrderStatus.COMPLETED);
        }

        return null;
    }

    public void cancelOrderByUserId(Long userId, Long orderId) {
        Order order = orderRepository.findFirstByOrderId(orderId);
        
        if(order == null) return;
        if(!order.getUserId().equals(userId)) return;
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return;
  
        if(order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        order.setStatus(OrderStatus.CANCELLED);

        ShipmentTracking st = order.getShipmentTracking();
        if(st != null) {
            shipmentTrackingService.updateStatus(order, ShipmentTrackingStatus.CANCELLED);
        }

        orderRepository.save(order);
    }



    public double calculateSubtotalPrice(Order order){
        double price = 0;

        for(OrderItem item : order.getItems()){
            price = price + item.getTotalPrice();
        }

        return price;
    }
    public double calculateTotalPrice(Order order){
        double price = calculateSubtotalPrice(order);
        double vat = price * 0.07; // Vat 7%;
        price = price + vat;

        return price;
    }
}

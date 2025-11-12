package tu_store.demo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.OrderDraftResponse;
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

    @Autowired
    private OrderStatusLogService orderStatusLogService;

    @Transactional
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

            orderStatusLogService.createOrderLog(order, null, OrderStatus.PENDING);
        }
        return order;
    }


    public List<Order> createOrdersResponseByUserId(Long id){
        return orderRepository.findAllByUserUserId(id);
    }

    @Transactional
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

    @Transactional
    public Order updateStatus(Order order, OrderStatus status){
        if(order == null || status == null) return null;
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return null;


        if(status == OrderStatus.PAID) {
            orderStatusLogService.createOrderLog(order, status);
            order.setStatus(status);

            ShipmentTracking st = shipmentTrackingService.getOrCreateShipmentTracking(order);
            order.setShipmentTracking(st);
        } else if(status == OrderStatus.COMPLETED) {
            if(order.getShipmentTracking() == null || order.getShipmentTracking().getStatus() != ShipmentTrackingStatus.DELIVERED){
                return null;
            }

            orderStatusLogService.createOrderLog(order, status);
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

    @Transactional
    public void cancelOrderByUserId(Long userId, Long orderId) {
        Order order = orderRepository.findFirstByOrderId(orderId);
        
        if(order == null) return;
        if(!order.getUserId().equals(userId)) return;
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return;
  
        if(order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        orderStatusLogService.createOrderLog(order, OrderStatus.CANCELLED);
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

    public OrderDraftResponse createOrderDraftResponse(Order order) {
        if (order == null) return null;

        OrderDraftResponse resp = new OrderDraftResponse();
        resp.setOrderId(order.getOrderId());
        resp.setStatus(order.getStatus() != null ? order.getStatus().name() : "DRAFT");

        List<CartItemDto> items = new ArrayList<>();
        int totalQuantity = 0;
        double totalAmount = 0.0;

        List<OrderItem> orderItems = order.getItems();
        if (orderItems != null) {
            for (OrderItem oi : orderItems) {
                CartItemDto dto = new CartItemDto();
                dto.setProductId(oi.getProductId());
                dto.setQuantity(oi.getQuantity());

                // compute unit price (OrderItem stores totalPrice)
                if (oi.getQuantity() > 0) {
                    long unitPrice = Math.round(oi.getTotalPrice() / oi.getQuantity());
                    dto.setPrice(unitPrice);
                } else {
                    dto.setPrice(0);
                }

                items.add(dto);

                totalQuantity += oi.getQuantity();
                totalAmount += oi.getTotalPrice();
            }
        }

        resp.setItems(items);
        resp.setTotalItems(items.size());
        resp.setTotalQuantity(totalQuantity);
        resp.setTotalAmount(totalAmount);

        return resp;
    }
}

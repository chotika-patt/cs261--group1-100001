package tu_store.demo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.transaction.Transactional;
import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.BuyerOrderItemResponse;
import tu_store.demo.dto.BuyerOrderResponse;
import tu_store.demo.dto.ShipmentTrackingResponse;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.dto.SellerOrderItemResponse;
import tu_store.demo.dto.SellerOrderResponse;
import tu_store.demo.dto.SellerOrderSummaryResponse;
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
    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    @Autowired private NotificationService notificationService;

    public Order getOrderByCartId(Long cartId) {
        return orderRepository.findFirstByCartCartId(cartId);
    }

    // -----------------------------------------------------
    // CREATE ORDER
    // -----------------------------------------------------
    @Transactional
    public Order createOrder(Cart cart){
        Order order = orderRepository.findFirstByCartCartId(cart.getCartId());
        if (order != null) {
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED) {
                order = null; // force new order creation
            } else {
                return order;
            }
        }

        if(order == null){

            order = new Order(cart);

            // set buyer from cart
            order.setBuyer(cart.getUser());

            // สินค้าทุกชิ้นในตะกร้านี้มาจาก seller ไหน?
            // → ใช้ seller จาก product ตัวแรกของ cart
            if (!cart.getItems().isEmpty()) {
                User seller = cart.getItems().get(0).getProduct().getSeller();
                order.setSeller(seller);
            }

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
    // -----------------------------------------------------
    // CREATE BUYER ORDER RESPONSE FOR CONTROLLER
    // -----------------------------------------------------
    public BuyerOrderResponse createClientOrderResponse(Order order){
        if(order == null) return null;
        BuyerOrderResponse response = new BuyerOrderResponse();
        response.setCreatedAt(order.getCreatedAt());
        response.setOrderId(order.getOrderId());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setShopName(order.getSeller().getOrganizationType());
        response.setProductName(order.getItems().get(0).getProduct().getName());
        response.setImagePath(order.getItems().get(0).getProduct().getMain_image());

        ShipmentTracking st = order.getShipmentTracking();
        if(st != null){
            response.setTrackingCode(st.getTrackingNumber());
            response.setsTrackingStatus(st.getStatus());
        }
        else{
            response.setTrackingCode("");
            response.setsTrackingStatus(null);
        }
        
        Integer q = 0;


        List<BuyerOrderItemResponse> items = new ArrayList<>();

        for(OrderItem item : order.getItems()){
            BuyerOrderItemResponse itemDto = orderItemService.createClientOrderItemResponse(item);
            items.add(itemDto);

            q = itemDto.getQuantity() + q;
        }

        response.setQuantity(q);
        response.setItems(items);

        return response;
    }
    public BuyerOrderResponse createClientOrderResponseByIdAndUserId(Long id, Long userId){
        Order order = orderRepository.findFirstByOrderIdAndBuyerUserId(id, userId);
        if(order == null) return null;

        return createClientOrderResponse(order);
    }
    public Page<BuyerOrderResponse> getClientOrderPageResponse(Long clientId, Pageable pageable){
        Page<Order> orders;

        orders = orderRepository.findAllByBuyerUserId(clientId, pageable);

        return orders.map(this::createClientOrderResponse);
    }
    public Page<BuyerOrderResponse> getClientOrderPageResponseWithStatus(Long clientId, String status, Pageable pageable){
        if(status == null) return getClientOrderPageResponse(clientId, pageable) ;

        Page<Order> orders;

        orders = orderRepository.findAllByBuyerUserIdAndStatus(clientId, status.toUpperCase(), pageable);

        return orders.map(this::createClientOrderResponse);
    }

    public ShipmentTrackingResponse getClientShipmentTrackingResponseByOrderIdAndUserId(Long id, Long userId){
        return shipmentTrackingService.createClientShipmentTrackingResponseByOrderIdAndUserId(id, userId);
    }

    public Page<BuyerOrderResponse> getClientOrderPageResponseWithFilters(
        Long userId,
        String search,
        OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ){
        Page<Order> orders;

        orders = orderRepository.findAllByBuyerIdWithFilters(userId, search, status, startDate, endDate, pageable);

        return orders.map(this::createClientOrderResponse);
    }

    // -----------------------------------------------------
    // CREATE SELLER ORDER RESPONSE FOR CONTROLLER
    // -----------------------------------------------------
    public SellerOrderResponse createSellerOrderResponse(Order order){
        if(order == null) return null;
        SellerOrderResponse response = new SellerOrderResponse();
        response.setCreatedAt(order.getCreatedAt());
        response.setOrderId(order.getOrderId());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());

        User buyer = order.getBuyer();
        if(buyer != null) response.setBuyerId(order.getBuyer().getUser_id());

        ShipmentTracking st = order.getShipmentTracking();
        if(st != null){
            response.setTrackingCode(st.getTrackingNumber());
        }
        else{
            response.setTrackingCode("");
        }
        
        Integer q = 0;


        List<SellerOrderItemResponse> items = new ArrayList<>();

        for(OrderItem item : order.getItems()){
            SellerOrderItemResponse itemDto = orderItemService.createSellerOrderItemResponse(item);
            items.add(itemDto);

            q = itemDto.getQuantity() + q;
        }

        response.setQuantity(q);
        response.setItems(items);

        return response;
    }
    public SellerOrderResponse createSellerOrderResponseByIdAndUserId(Long id, Long userId){
        Order order = orderRepository.findFirstByOrderIdAndSellerUserId(id, userId);
        if(order == null) return null;

        return createSellerOrderResponse(order);
    }
    public Page<SellerOrderResponse> getSellerOrderPageResponse(Long clientId, Pageable pageable){
        Page<Order> orders;

        orders = orderRepository.findAllBySellerUserId(clientId, pageable);

        return orders.map(this::createSellerOrderResponse);
    }
    public Page<SellerOrderResponse> getSellerOrderPageResponseWithStatus(Long clientId, String status, Pageable pageable){
        if(status == null) return getSellerOrderPageResponse(clientId, pageable) ;

        Page<Order> orders;

        orders = orderRepository.findAllByBuyerUserIdAndStatus(clientId, status.toUpperCase(), pageable);

        return orders.map(this::createSellerOrderResponse);
    }

    public ShipmentTrackingResponse getSellerShipmentTrackingResponseByOrderIdAndUserId(Long id, Long userId){
        return shipmentTrackingService.createSellerShipmentTrackingResponseByOrderIdAndUserId(id, userId);
    }

    public Page<SellerOrderResponse> getSellerOrderPageResponseWithFilters(
        Long userId,
        String search,
        OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ){
        Page<Order> orders;

        orders = orderRepository.findAllBySellerIdWithFilters(userId, search, status, startDate, endDate, pageable);

        return orders.map(this::createSellerOrderResponse);
    }
    public SellerOrderSummaryResponse createSellerOrderSummaryResponseBySellerIdWithFilters(
        Long userId,
        String search,
        OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate)
    {
        SellerOrderSummaryResponse summary = new SellerOrderSummaryResponse();
        Double totalSales = 0.0;
        Double pendingPayments = 0.0;

        if(status == null){
            totalSales = orderRepository.totalPriceFiltered(userId, search, OrderStatus.COMPLETED, startDate, endDate);
            pendingPayments = orderRepository.totalPriceFiltered(userId, search, OrderStatus.PENDING, startDate, endDate);
        }else if(status == OrderStatus.COMPLETED){
            totalSales = orderRepository.totalPriceFiltered(userId, search, OrderStatus.COMPLETED, startDate, endDate);
        }else if(status == OrderStatus.PENDING){
            pendingPayments = orderRepository.totalPriceFiltered(userId, search, OrderStatus.PENDING, startDate, endDate);
        }

        summary.setTotalOrders(orderRepository.totalOrdersFiltered(userId, search, status, startDate, endDate));
        summary.setTotalSales(totalSales);
        summary.setPendingPayments(pendingPayments);
        return summary;
    }


    // -----------------------------------------------------
    // GET ORDERS BY BUYER
    // -----------------------------------------------------
    public List<Order> createOrdersResponseByUserId(Long id){
        return orderRepository.findAllByBuyerUserId(id);
    }

    // -----------------------------------------------------
    // CHECKOUT
    // -----------------------------------------------------
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

    // -----------------------------------------------------
    // GET ORDER ENTITY
    // -----------------------------------------------------
    public Order getOrderById(Long id){
        return orderRepository.findFirstByOrderId(id);
    }

    public OrderStatus getOrderStatusById(Long id){
        Order order = orderRepository.findFirstByOrderId(id);
        if(order == null) return null;

        return order.getStatus();
    }


    // -----------------------------------------------------
    // UPDATE STATUS
    // -----------------------------------------------------
    public Boolean updateStatus(Order order, ShipmentTrackingStatus status){
        if(order == null || status == null) return null;
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return false;
        
        ShipmentTracking st = order.getShipmentTracking();
        if(st == null) return false;

        ShipmentTrackingStatus oldStatus = st.getStatus();

        if(status == ShipmentTrackingStatus.SHIPPED) {
            if(oldStatus != ShipmentTrackingStatus.PREPARING) return false;
            
            shipmentTrackingService.updateStatus(order, status);
            //notificationService.sendOrderStatusUpdateByEmail(order, oldStatus, status);

        } else if(status == ShipmentTrackingStatus.DELIVERED) {
            if(oldStatus != ShipmentTrackingStatus.SHIPPED) return false;

            shipmentTrackingService.updateStatus(order, status);

            //notificationService.sendOrderStatusUpdateByEmail(order, oldStatus, status);
            orderStatusLogService.createOrderLog(order, OrderStatus.COMPLETED);
            order.setStatus(OrderStatus.COMPLETED);
        }

        orderRepository.save(order);
        return true;
    }


    public void updateStatusTest(Order order, OrderStatus status){
        if(order == null || status == null) return;

        orderStatusLogService.createOrderLog(order, status);
        order.setStatus(status);


        if (order.getStatus() == OrderStatus.PAID) {
            ShipmentTracking stExisting = order.getShipmentTracking();
            if (stExisting == null) {
                ShipmentTracking st = shipmentTrackingService.getOrCreateShipmentTracking(order);
                order.setShipmentTracking(st);
                orderRepository.save(order);
            }
            return;
        }
    }


    // -----------------------------------------------------
    // CANCEL ORDER
    // -----------------------------------------------------
    @Transactional
    public void cancelOrderByUserId(Long userId, Long orderId) {
        Order order = orderRepository.findFirstByOrderId(orderId);

        if(order == null) return;
        if(!order.getBuyer().getUser_id().equals(userId)) return;
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return;

        orderStatusLogService.createOrderLog(order, OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);

        ShipmentTracking st = order.getShipmentTracking();
        if(st != null) {
            //ShipmentTrackingStatus oldStStatus = st.getStatus();

            shipmentTrackingService.updateStatus(order, ShipmentTrackingStatus.CANCELLED);
            //notificationService.sendOrderStatusUpdateByEmail(order, oldStStatus, ShipmentTrackingStatus.CANCELLED);
        }

        orderRepository.save(order);
    }

    // -----------------------------------------------------
    // PRICE CALCULATIONS
    // -----------------------------------------------------
    public double calculateSubtotalPrice(Order order){
        double price = 0;
        for(OrderItem item : order.getItems()){
            price += item.getTotalPrice();
        }
        return price;
    }

    public double calculateTotalPrice(Order order){
        double price = calculateSubtotalPrice(order);
        double vat = price * 0.07; // Vat 7%
        return price + vat;
    }

    // -----------------------------------------------------
    // ORDER DRAFT RESPONSE
    // -----------------------------------------------------
    public OrderDraftResponse createOrderDraftResponse(Order order) {
        if (order == null) return null;

        OrderDraftResponse resp = new OrderDraftResponse();
        resp.setOrderId(order.getOrderId());
        resp.setStatus(order.getStatus() != null ? order.getStatus().name() : "DRAFT");

        List<CartItemDto> items = new ArrayList<>();
        int totalQuantity = 0;
        double totalAmount = 0.0;

        if (order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {

                CartItemDto dto = new CartItemDto();
                dto.setProductId(oi.getProductId());
                dto.setQuantity(oi.getQuantity());

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

    @Transactional
    public void updateStatusToPaid(Long orderId, String paymentRef) {
        if (orderId == null) throw new IllegalArgumentException("orderId required");

        Order order = orderRepository.findFirstByOrderId(orderId);
        if (order == null) throw new IllegalArgumentException("order not found");

        // Debug log entry
        System.out.println("[ORDER] updateStatusToPaid() called for orderId=" + orderId + " currentStatus=" + order.getStatus() + " paymentRef=" + paymentRef);

        // Prevent invalid transitions
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("cannot mark paid on cancelled/completed order");
        }

        // If already PAID, ensure shipmentTracking exists and return
        if (order.getStatus() == OrderStatus.PAID) {
            ShipmentTracking stExisting = order.getShipmentTracking();
            if (stExisting == null) {
                ShipmentTracking st = shipmentTrackingService.getOrCreateShipmentTracking(order);
                order.setShipmentTracking(st);
                orderRepository.saveAndFlush(order);

                //notificationService.sendOrderStatusUpdateByEmail(order, null, st.getStatus());
            }
            System.out.println("[ORDER] already PAID - nothing to do for orderId=" + orderId);
            return;
        }

        // capture previous status for logging
        OrderStatus prevStatus = order.getStatus();

        // 1) Update order status to PAID and flush
        order.setStatus(OrderStatus.PAID);
        orderRepository.saveAndFlush(order);
        System.out.println("[ORDER] order status updated to PAID (orderId=" + orderId + ")");

        // 2) Ensure shipment tracking exists (will not throw now that order is PAID)
        try {
            ShipmentTracking st = shipmentTrackingService.getOrCreateShipmentTracking(order);
            order.setShipmentTracking(st);
            orderRepository.saveAndFlush(order);

            //notificationService.sendOrderStatusUpdateByEmail(order, null, st.getStatus());
        } catch (Exception e) {
            // log the exception but do not abort — still proceed to stock update and logging
            System.err.println("[ORDER] failed to create shipment tracking for orderId=" + orderId + " : " + e.getMessage());
            e.printStackTrace();
        }

        // 3) Create status log using explicit prev/new
        try {
            orderStatusLogService.createOrderLog(order, prevStatus, OrderStatus.PAID);
        } catch (Exception e) {
            System.err.println("[ORDER] failed to create order status log for orderId=" + orderId + ": " + e.getMessage());
            e.printStackTrace();
        }

        // 4) Deduct stock for items (defensive checks)
        try {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product == null) continue;
                int remaining = product.getStock() - item.getQuantity();
                if (remaining < 0) remaining = 0;
                product.setStock(remaining);
                productRepository.save(product);
            }
            // final save of order (if any relationships changed)
            orderRepository.saveAndFlush(order);
        } catch (Exception e) {
            System.err.println("[ORDER] error updating stock for orderId=" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // rethrow so caller (webhook) can log ORDER_UPDATE_FAILED visibly
        }

        System.out.println("[ORDER] updateStatusToPaid completed for orderId=" + orderId);
    }

    @Transactional
    public boolean applySellerDecision(Order order, String decision, Long sellerUserId){
        if(order == null || decision == null) return false;
        if(order.getSeller() == null || !order.getSeller().getUser_id().equals(sellerUserId)) return false;
        // cannot act on completed/cancelled orders
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) return false;

        // Basic decision handling
        if("ACCEPT".equalsIgnoreCase(decision)) {
            OrderStatus prev = order.getStatus();
            order.setStatus(OrderStatus.PAID);
            orderRepository.saveAndFlush(order);

            // create shipment tracking if not exists
            try {
                ShipmentTracking st = shipmentTrackingService.getOrCreateShipmentTracking(order);
                order.setShipmentTracking(st);
                orderRepository.saveAndFlush(order);
            } catch(Exception e) {
                // log but continue
                System.err.println("[ORDER] failed to create shipment tracking: " + e.getMessage());
            }

            // create status log
            orderStatusLogService.createOrderLog(order, prev, order.getStatus());

            return true;
        } else if ("REJECT".equalsIgnoreCase(decision)) {
            // mark cancelled and log
            OrderStatus prev = order.getStatus();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.saveAndFlush(order);

            // if shipment tracking exists, mark cancelled too
            ShipmentTracking st = order.getShipmentTracking();
            if(st != null) {
                shipmentTrackingService.updateStatus(order, ShipmentTrackingStatus.CANCELLED);
            }

            orderStatusLogService.createOrderLog(order, prev, OrderStatus.CANCELLED);

            return true;
        }

        return false;
    }
}

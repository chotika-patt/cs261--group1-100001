package tu_store.demo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tu_store.demo.models.*;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;
import tu_store.demo.repositories.*;

@Service
public class ShipmentTrackingService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShipmentTrackingRepository shipmentTrackingRepository;

    @Transactional
    public ShipmentTracking getOrCreateShipmentTracking(Order order){
        if(order == null) return null;
        if(order.getStatus() == OrderStatus.PENDING) throw new IllegalStateException("Cannot update shipment before payment");

        ShipmentTracking st = order.getShipmentTracking();

        if(st != null) return st;

        st = new ShipmentTracking();
        st.setOrder(order);
        st.setTrackingNumber(generateTrackingNumber(order));

        order.setShipmentTracking(st);

        shipmentTrackingRepository.save(st);
        return st;
    }

    @Transactional
    public ShipmentTracking updateStatus(Order order, ShipmentTrackingStatus status) {
        ShipmentTracking st = order.getShipmentTracking();
        if(st == null) return null;

        st.setStatus(status);

        if(order.getStatus() == OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot update shipment before payment");
        }

        if(status == ShipmentTrackingStatus.SHIPPED) {
            st.setShippedAt(LocalDateTime.now());
        } else if(status == ShipmentTrackingStatus.DELIVERED) {
            st.setDeliveredAt(LocalDateTime.now());
        }

        orderRepository.save(order);
        return st;
    }
    @Transactional
    public ShipmentTracking updateStatus(Order order) {
        ShipmentTracking st = order.getShipmentTracking();
        if(st == null) return null;

        if(st.getStatus() == ShipmentTrackingStatus.PREPARING){
            return updateStatus(order, ShipmentTrackingStatus.SHIPPED);
        }
        else if(st.getStatus() == ShipmentTrackingStatus.SHIPPED){
            return updateStatus(order, ShipmentTrackingStatus.DELIVERED);
        }

        return null;
    }

    private String generateTrackingNumber(Order order) {
        return "TH-" + order.getOrderId() + "-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10);
    }
}

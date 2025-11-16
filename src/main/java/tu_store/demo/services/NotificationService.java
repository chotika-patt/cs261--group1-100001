package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import tu_store.demo.models.Order;
import tu_store.demo.models.User;
import tu_store.demo.models.enums.ShipmentTrackingStatus;


@Service
public class NotificationService {
    @Autowired
    private JavaMailSender mailSender;

    private String statusToText(ShipmentTrackingStatus status) {
        if (status == null) return "No Status";
        switch (status) {
            case PREPARING: return "Preparing";
            case SHIPPED: return "Shipped";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            default: return status.name();
        }
    }

    @Async
    public void sendOrderStatusUpdateByEmail(Order order, ShipmentTrackingStatus oldStatus, ShipmentTrackingStatus newStatus) {
        User user = order.getBuyer();
        String oldStatusText = "";
        String newStatusText = "";

        if (newStatus == null) return;
        if (oldStatus == ShipmentTrackingStatus.CANCELLED) return;

        oldStatusText = statusToText(oldStatus);
        newStatusText = statusToText(newStatus);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Order status updated!");
        message.setText(
            "Order Update\n\n" +
            "Order ID: " + order.getOrderId() + "\n" +
            "Status changed" + "\n" +
            "From: " + oldStatusText + "\n" +
            "To: " + newStatusText + "\n"
        );
        mailSender.send(message);
    }


}

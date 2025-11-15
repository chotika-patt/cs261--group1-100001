package tu_store.demo.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import tu_store.demo.dto.InitiatePaymentRequest;
import tu_store.demo.services.PaymentService;
import tu_store.demo.services.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(HttpSession session, @RequestBody InitiatePaymentRequest req) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null) return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode", "NOT_FOUND", "message", "Please login"));
        try {
            var resp = paymentService.initiatePayment(req, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("errorCode","INVALID_PARAM","message",e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorCode","INTERNAL_ERROR","message","Could not initiate payment"));
        }
    }

    @PostMapping("/{paymentId:\\d+}")
    public ResponseEntity<?> getStatus(HttpSession session, @PathVariable Long paymentId) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode", "NOT_FOUND", "message", "Please login"));

        try {
            var s = paymentService.getStatus(paymentId, userId);
            return ResponseEntity.ok(s);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorCode", "NOT_FOUND", "message", "payment not found"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode", "UNAUTHORIZED", "message", "not your payment"));
        }
    }

    @PostMapping("/{paymentId:\\d+}/cancel")
    public ResponseEntity<?> cancel(HttpSession session, @PathVariable Long paymentId) {
        Long userId = userService.getUserIdBySession(session);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode","UNAUTHORIZED","message","Please login"));
        try {
            paymentService.cancelPayment(paymentId, userId);
            return ResponseEntity.ok(Map.of("message","cancelled"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorCode","NOT_FOUND","message","payment not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorCode","INVALID_STATE","message",e.getMessage()));
        }
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<?> webhook(@PathVariable String provider, @RequestHeader(value="X-Signature", required = false) String sig, @RequestBody String body) {
        return paymentService.handleWebhook(provider, sig, body);
    }
}

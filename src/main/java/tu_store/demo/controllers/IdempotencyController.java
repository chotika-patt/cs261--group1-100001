package tu_store.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/idempotency")
public class IdempotencyController {

    @GetMapping("/generate")
    public ResponseEntity<Map<String,String>> generate() {
        String key = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("idempotencyKey", key));
    }
}

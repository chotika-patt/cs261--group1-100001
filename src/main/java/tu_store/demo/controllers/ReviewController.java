package tu_store.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.CartDto;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.models.*;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ReviewService;
import tu_store.demo.services.UserService;
import tu_store.demo.services.CartItemService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/review")
public class ReviewController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/addTest")
    public ResponseEntity<?> addTest(HttpSession session, @RequestBody ReviewRequest reviewRequest){

        reviewService.createReview(reviewRequest);
        

        return ResponseEntity.ok(getReviewTest(session));
    }

    @GetMapping("/getReviewsTest")
    public ResponseEntity<?> getReviewTest(HttpSession session) {

        return ResponseEntity.ok(reviewService.getLatest100AndCreateResponses());
    }
    
}
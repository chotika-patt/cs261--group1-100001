package tu_store.demo.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class AddToCartController {
    @GetMapping("/add")
    public String add(@RequestParam String items){
        return "Item Name : " + items;
    }
    
}

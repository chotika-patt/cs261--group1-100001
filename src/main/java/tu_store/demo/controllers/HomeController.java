package tu_store.demo.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class HomeController {
    @GetMapping("test-param")
    public String getMethodName(@RequestParam(name = "name") String nameParam,@RequestParam(name = "age") int ageParam) {
        return "name : " + nameParam + " " + ageParam;
    }

}

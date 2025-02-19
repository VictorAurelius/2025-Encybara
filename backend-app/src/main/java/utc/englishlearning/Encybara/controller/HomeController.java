package utc.englishlearning.Encybara.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Trả về view login.jsp mà không cần phần mở rộng
    }

}

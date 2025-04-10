package ch.modul295.yannisstebler.financeapp.swaggerUI;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SwaggerController {
    @RequestMapping("/swagger")
    public String home() {
        return "redirect:/api/swagger-ui.html";
    }
}
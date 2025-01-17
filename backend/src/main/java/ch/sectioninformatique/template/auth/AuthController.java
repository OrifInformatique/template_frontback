package ch.sectioninformatique.template.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthController {

    @GetMapping("/success")
    public String onLoginSuccess() {
        return "Successful login with spring security !";
    }
}

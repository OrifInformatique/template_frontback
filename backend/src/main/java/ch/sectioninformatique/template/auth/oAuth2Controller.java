package ch.sectioninformatique.template.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/oauth2")
@RestController
public class oAuth2Controller {

    @GetMapping("/success")
    public String onAzureLoginSuccess() {
        // TODO: redirect to REACT frontend
        return "Successful login with azure !";
    }
}


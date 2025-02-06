package ch.sectioninformatique.template.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/oauth2")
@RestController
public class oAuth2Controller {

    @GetMapping("/success")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal OAuth2User principal,
            HttpServletResponse response) {
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication();
        String accessToken = ((OAuth2AuthorizedClient) authentication.getPrincipal()).getAccessToken().getTokenValue();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", principal.getAttributes());
        responseData.put("token", accessToken);

        return ResponseEntity.ok(responseData);
    }

}

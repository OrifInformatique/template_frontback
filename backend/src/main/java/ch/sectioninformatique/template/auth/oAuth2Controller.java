package ch.sectioninformatique.template.auth;

import ch.sectioninformatique.template.user.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestMapping("/oauth2")
@RestController
public class Oauth2Controller {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserAuthenticationProvider userAuthenticationProvider;
    private static final Logger log = LoggerFactory.getLogger(Oauth2Controller.class);

    public Oauth2Controller(OAuth2AuthorizedClientService authorizedClientService,
                            UserAuthenticationProvider userAuthenticationProvider) {
        this.authorizedClientService = authorizedClientService;
        this.userAuthenticationProvider = userAuthenticationProvider;
    }

    @GetMapping("/success")
    public void oauth2Success(OAuth2AuthenticationToken authentication, HttpServletResponse response) throws IOException {
        if (authentication == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication token is missing.");
            return;
        }

        // Retrieve OAuth2User principal from the authentication token.
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        if (principal == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "OAuth2 user details not found.");
            return;
        }

        // Map OAuth2User attributes to your UserDto.
        // Adjust the attribute keys as needed based on your Azure configuration.
        String email = principal.getAttribute("email");
        String givenName = principal.getAttribute("given_name");
        String familyName = principal.getAttribute("family_name");

        if (Objects.isNull(email)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Required user attribute not found.");
            return;
        }

        UserDto user = UserDto.builder()
                .login(email)
                .firstName(givenName)
                .lastName(familyName)
                .build();

        // Generate a JWT using your custom UserAuthenticationProvider.
        String jwt = userAuthenticationProvider.createToken(user);

        // Construct a redirect URL for your frontend with the token
        String redirectUrl = String.format("http://localhost:4000/oauth2/success?token=%s&loginType=azure",
                URLEncoder.encode(jwt, StandardCharsets.UTF_8));

        log.debug("Redirecting to frontend with JWT token");
        response.sendRedirect(redirectUrl);
    }
}

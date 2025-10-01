package ch.sectioninformatique.template.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

@Service
public class AuthClient {

    private final WebClient webClient;

    public AuthClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> login(String login, char[] password) {

        return webClient.post()
                .uri("/auth/login") // your login endpoint path
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(login, password)) // assuming LoginRequest is a class with login and password fields
                .retrieve()
                .bodyToMono(String.class); // expect the response as a String (e.g., a token or message)
    }

    public Mono<String> loginOAUth2() {

        return webClient.get()
                .uri("/oauth2/authorization/azure") // your login endpoint path
                .retrieve()
                .bodyToMono(String.class); // expect the response as a String (e.g., a token or message)
    }
}



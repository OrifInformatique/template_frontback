package ch.sectioninformatique.template.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

/**
 * Client service for authentication operations.
 * This service provides methods to interact with the authentication endpoints,
 * including login and registration functionalities.
 * It uses WebClient to perform HTTP requests and handle responses reactively.
 */
@Service
public class AuthClient {

    /** WebClient instance for making HTTP requests */
    private final WebClient webClient;

    /** Constructor to initialize the WebClient */
    public AuthClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /** 
     * Performs user login by sending credentials to the authentication endpoint.
     * 
     * @param login The user's login identifier
     * @param password The user's password as a character array
     * @return A Mono<String> containing the authentication response (e.g., token or status message)
     */
    public Mono<String> login(String login, char[] password) {

        return webClient.post()
                .uri("/auth/login") // your login endpoint path
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CredentialsDto(login, password)) // assuming LoginRequest is a class with login and password fields
                .retrieve()
                .bodyToMono(String.class); // expect the response as a String (e.g., a token or message)
    }

    /** 
     * Performs user registration by sending user details to the registration endpoint.
     * 
     * @param user The SignUpDto containing user registration data
     * @return A Mono<String> containing the registration response (e.g., token or status message)
     */
    public Mono<String> register(SignUpDto user) {

        return webClient.post()
                .uri("/auth/register") // your register endpoint path
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user) // use the SignUpDto directly
                .retrieve()
                .bodyToMono(String.class); // expect the response as a String (e.g., a token or message)
    }

    /** 
     * Initiates OAuth2 login by redirecting to the OAuth2 authorization endpoint.
     * 
     * @return A Mono<String> containing the OAuth2 login response (e.g., token or status message)
     */
    public Mono<String> loginOAUth2() {

        return webClient.get()
                .uri("/oauth2/authorization/azure") // your login endpoint path
                .retrieve()
                .bodyToMono(String.class); // expect the response as a String (e.g., a token or message)
    }
}



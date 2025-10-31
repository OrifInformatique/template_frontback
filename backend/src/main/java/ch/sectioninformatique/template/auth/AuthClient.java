package ch.sectioninformatique.template.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Client service for authentication operations.
 * This service provides methods to interact with the spring-auth application's
 * endpoints,
 * including login and registration functionalities.
 * It uses WebClient to perform HTTP requests and handle responses reactively.
 */
@Service
public class AuthClient {

    /** WebClient instance for making HTTP requests */
    private final WebClient webClient;

    /** Constructor to initialize the WebClient */
    public AuthClient(WebClient webClient) {
        /**
         * TODO : We should externalize the URL in a configuration file or environment
         * variable
         * Attention : don't use https://auth.sectioninformatique.ch beacause the SSL
         * certificate is not valid for this domain
         * Use https://spring-auth.jcloud.ik-server.com/ as it is the Jelastic server
         * domain name, with a valid SSL certificate
         */
        this.webClient = WebClient.create("https://spring-auth.jcloud.ik-server.com/ ");
    }

    /**
     * Performs user login by sending credentials to the authentication endpoint.
     * 
     * @param credentialsDto The CredentialsDto containing user login data
     * @return A Mono<ResponseEntity<String>> containing the authentication response (e.g., token or
     *         status message)
     */
    public Mono<ResponseEntity<String>> login(@Valid CredentialsDto credentialsDto) {

        return webClient.post()
                .uri("/auth/login") // login endpoint path in spring-auth application
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentialsDto)
                .retrieve()
                .toEntity(String.class); // expect the response as a ResponseEntity<String> (e.g., a token or message)

    }

    /**
     * Performs user registration by sending user details to the registration
     * endpoint.
     * 
     * @param user The SignUpDto containing user registration data
     * @return A Mono<ResponseEntity<String>> containing the registration response (e.g., token or
     *         status message)
     */
    public Mono<ResponseEntity<String>> register(SignUpDto user) {

        return webClient.post()
                .uri("/auth/register") // your register endpoint path
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user) // use the SignUpDto directly
                .retrieve()
                .toEntity(String.class); // expect the response as a ResponseEntity<String> (e.g., a token or message);
    }

    /**
     * Initiates OAuth2 login by redirecting to the OAuth2 authorization endpoint.
     * 
     * @return A Mono<ResponseEntity<String>> containing the OAuth2 login response (e.g., token or
     *         status message)
     */
    public Mono<ResponseEntity<String>> loginOAUth2() {

        return webClient.get()
                .uri("/oauth2/authorization/azure") // your login endpoint path
                .retrieve()
                .toEntity(String.class); // expect the response as a ResponseEntity<String> (e.g., a token or message)
    }
}

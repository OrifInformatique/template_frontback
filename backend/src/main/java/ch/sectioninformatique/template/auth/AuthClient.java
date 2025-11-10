package ch.sectioninformatique.template.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ch.sectioninformatique.template.app.errors.ErrorDto;
import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.user.UserDto;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
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
        public AuthClient(@Value("${SPRING_AUTH_URL}") String authUrl) {
                this.webClient = WebClient.create(authUrl);
        }

        /**
         * Performs user login by sending credentials to the authentication endpoint.
         * 
         * @param credentialsDto The CredentialsDto containing user login data
         * @return A Mono<ResponseEntity<UserDto>> containing the authentication
         *         response
         *         (e.g., token or
         *         status message)
         */
        public Mono<ResponseEntity<UserDto>> login(@Valid CredentialsDto credentialsDto) {

                return webClient.post()
                                .uri("/auth/login") // login endpoint path in spring-auth application
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(credentialsDto)
                                .retrieve()
                                .onStatus(status -> status.value() >= 400, // any 4xx/5xx
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode())))))
                                .toEntity(UserDto.class); // expect the response as a ResponseEntity<String> (e.g., a
                                                          // token or message)

        }

        /**
         * Performs user registration by sending user details to the registration
         * endpoint.
         * 
         * @param user The SignUpDto containing user registration data
         * @return A Mono<ResponseEntity<UserDto>> containing the registration response
         *         (e.g., token or
         *         status message)
         */
        public Mono<ResponseEntity<UserDto>> register(RegisterDto user) {

                return webClient.post()
                                .uri("/auth/register") // your register endpoint path
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(user) // use the SignUpDto directly
                                .retrieve()
                                .onStatus(status -> status.value() >= 400, // any 4xx/5xx
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode())))))
                                .toEntity(UserDto.class); // expect the response as a ResponseEntity<String> (e.g., a
                                                          // token or message);
        }

        /**
         * Sets a new password for the user by sending the new password to the set
         * password endpoint.
         * 
         * @param token           The authorization token
         * @param newPasswordDto The NewPasswordDto containing the new password data
         * @return A Mono<ResponseEntity<MessageResponseDto>> containing the set
         *         password response
         *         (e.g., token or
         *         status message)
         */
        public Mono<ResponseEntity<MessageResponseDto>> setPassword(String token, NewPasswordDto newPasswordDto) {

                return webClient.put()
                                .uri("/auth/set-password") // your set-password endpoint path
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .bodyValue(newPasswordDto) // use the NewPasswordDto directly
                                .retrieve()
                                .onStatus(status -> status.value() >= 400, // any 4xx/5xx
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode())))))
                                .toEntity(MessageResponseDto.class); // expect the response as a ResponseEntity<String> (e.g., a
                                                          // token or message);
        }

        /**
         * Initiates OAuth2 login by redirecting to the OAuth2 authorization endpoint.
         * 
         * @return A Mono<ResponseEntity<String>> containing the OAuth2 login response
         *         (e.g., token or
         *         status message)
         */
        public Mono<ResponseEntity<String>> loginOAUth2() {

                return webClient.get()
                                .uri("/oauth2/authorization/azure") // your login endpoint path
                                .retrieve()
                                .toEntity(String.class); // expect the response as a ResponseEntity<String> (e.g., a
                                                         // token or message)
        }
}

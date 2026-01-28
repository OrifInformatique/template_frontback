package ch.sectioninformatique.template.user;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import ch.sectioninformatique.template.app.errors.ErrorDto;
import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.CredentialsDto;
import ch.sectioninformatique.template.user.UserDto;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Service client for handling user-related HTTP requests to the authentication service.
 * This class uses Spring WebClient to communicate with the authentication microservice
 * and perform user management operations such as promoting users to manager role.
 */
@Service
public class UserClient {
    /** WebClient instance for making HTTP requests to the authentication service */
    private final WebClient webClient;

    /**
     * Constructor to initialize the WebClient with the authentication service URL.
     * 
     * @param authUrl the base URL of the authentication service, injected from application properties
     */
    public UserClient(@Value("${SPRING_AUTH_URL}") String authUrl) {
        this.webClient = WebClient.create(authUrl);
    }

    /**
     * Promotes a user to manager role by sending a PUT request to the authentication service.
     * This method makes an asynchronous HTTP call and handles potential errors by converting
     * error responses into AppException instances.
     * 
     * @param token the authorization token (Bearer token) to authenticate the request
     * @param userId the ID of the user to be promoted to manager role
     * @return a Mono containing the ResponseEntity with the operation result
     * @throws AppException if the authentication service returns an error status (4xx or 5xx)
     */
    public Mono<ResponseEntity<String>> promoteToManager(String token, Long userId) {
        return webClient.put()
                // Construct the URI with the user ID to target the specific user
                .uri("/users/" + userId + "/promote-manager")
                // Add the authorization token to the request headers
                .header(HttpHeaders.AUTHORIZATION, token)
                // Execute the HTTP request
                .retrieve()
                // Handle error responses (4xx and 5xx status codes)
                .onStatus(status -> status.value() >= 400,
                        response -> response.bodyToMono(ErrorDto.class)
                                // Convert error response body to ErrorDto and wrap in AppException
                                .flatMap(error -> Mono.error(
                                        new AppException(error.message(),
                                                HttpStatus.resolve(
                                                        response.statusCode().value())))))
                // Convert the response to a ResponseEntity
                .toEntity(String.class);
    }

    /**
     * Revokes manager role from a user by sending a PUT request to the authentication service.
     * This method makes an asynchronous HTTP call and handles potential errors by converting
     * error responses into AppException instances.
     * 
     * @param token the authorization token (Bearer token) to authenticate the request
     * @param userId the ID of the user whose manager role will be revoked
     * @return a Mono containing the ResponseEntity with the operation result
     * @throws AppException if the authentication service returns an error status (4xx or 5xx)
     */
    public Mono<ResponseEntity<String>> revokeManager(String token, Long userId) {
        return webClient.put()
                // Construct the URI with the user ID to target the specific user
                .uri("/users/" + userId + "/revoke-manager")
                // Add the authorization token to the request headers
                .header(HttpHeaders.AUTHORIZATION, token)
                // Execute the HTTP request
                .retrieve()
                // Handle error responses (4xx and 5xx status codes)
                .onStatus(status -> status.value() >= 400,
                        response -> response.bodyToMono(ErrorDto.class)
                                // Convert error response body to ErrorDto and wrap in AppException
                                .flatMap(error -> Mono.error(
                                        new AppException(error.message(),
                                                HttpStatus.resolve(
                                                        response.statusCode().value())))))
                // Convert the response to a ResponseEntity
                .toEntity(String.class);
    }

    /**
     * Promotes a user to admin role by sending a PUT request to the authentication service.
     * This method makes an asynchronous HTTP call and handles potential errors by converting
     * error responses into AppException instances.
     * 
     * @param token the authorization token (Bearer token) to authenticate the request
     * @param userId the ID of the user to be promoted to admin role
     * @return a Mono containing the ResponseEntity with the operation result
     * @throws AppException if the authentication service returns an error status (4xx or 5xx)
     */
    public Mono<ResponseEntity<String>> promoteToAdmin(String token, Long userId) {
        return webClient.put()
                // Construct the URI with the user ID to target the specific user
                .uri("/users/" + userId + "/promote-admin")
                // Add the authorization token to the request headers
                .header(HttpHeaders.AUTHORIZATION, token)
                // Execute the HTTP request
                .retrieve()
                // Handle error responses (4xx and 5xx status codes)
                .onStatus(status -> status.value() >= 400,
                        response -> response.bodyToMono(ErrorDto.class)
                                // Convert error response body to ErrorDto and wrap in AppException
                                .flatMap(error -> Mono.error(
                                        new AppException(error.message(),
                                                HttpStatus.resolve(
                                                        response.statusCode().value())))))
                // Convert the response to a ResponseEntity
                .toEntity(String.class);
    }

    /**
     * Revokes admin role from a user by sending a PUT request to the authentication service.
     * This method makes an asynchronous HTTP call and handles potential errors by converting
     * error responses into AppException instances.
     * 
     * @param token the authorization token (Bearer token) to authenticate the request
     * @param userId the ID of the user whose admin role will be revoked
     * @return a Mono containing the ResponseEntity with the operation result
     * @throws AppException if the authentication service returns an error status (4xx or 5xx)
     */
    public Mono<ResponseEntity<String>> revokeAdmin(String token, Long userId) {
        return webClient.put()
                // Construct the URI with the user ID to target the specific user
                .uri("/users/" + userId + "/revoke-admin")
                // Add the authorization token to the request headers
                .header(HttpHeaders.AUTHORIZATION, token)
                // Execute the HTTP request
                .retrieve()
                // Handle error responses (4xx and 5xx status codes)
                .onStatus(status -> status.value() >= 400,
                        response -> response.bodyToMono(ErrorDto.class)
                                // Convert error response body to ErrorDto and wrap in AppException
                                .flatMap(error -> Mono.error(
                                        new AppException(error.message(),
                                                HttpStatus.resolve(
                                                        response.statusCode().value())))))
                // Convert the response to a ResponseEntity
                .toEntity(String.class);
    }
}

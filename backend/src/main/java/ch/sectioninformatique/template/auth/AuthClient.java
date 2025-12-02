package ch.sectioninformatique.template.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ch.sectioninformatique.template.app.errors.ErrorDto;
import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.user.UserDto;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(new AppException(
                                                                                error.message(),
                                                                                HttpStatus.resolve(response
                                                                                                .rawStatusCode()))));
                                        }

                                        // Extract response body
                                        Mono<UserDto> bodyMono = response.bodyToMono(UserDto.class);

                                        return bodyMono.map(userDto -> {
                                                ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

                                                // Extract refresh_token cookie directly in the lambda
                                                response.headers().asHttpHeaders()
                                                                .getOrDefault(HttpHeaders.SET_COOKIE,
                                                                                Collections.emptyList())
                                                                .stream()
                                                                .filter(cookie -> cookie.startsWith("refresh_token="))
                                                                .findFirst()
                                                                .ifPresent(cookie -> builder.header(
                                                                                HttpHeaders.SET_COOKIE, cookie));
                                                return builder.body(userDto);
                                        });
                                });

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
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(new AppException(
                                                                                error.message(),
                                                                                HttpStatus.resolve(response
                                                                                                .rawStatusCode()))));
                                        }

                                        // Extract response body
                                        Mono<UserDto> bodyMono = response.bodyToMono(UserDto.class);

                                        return bodyMono.map(userDto -> {
                                                ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

                                                // Extract refresh_token cookie directly in the lambda
                                                response.headers().asHttpHeaders()
                                                                .getOrDefault(HttpHeaders.SET_COOKIE,
                                                                                Collections.emptyList())
                                                                .stream()
                                                                .filter(cookie -> cookie.startsWith("refresh_token="))
                                                                .findFirst()
                                                                .ifPresent(cookie -> builder.header(
                                                                                HttpHeaders.SET_COOKIE, cookie));
                                                return builder.body(userDto);
                                        });
                                });
        }

        /**
         * Refresh the access token using a refresh token
         * 
         * @param token The access token
         * @return A Mono<ResponseEntity<TokenResponseDto>> containing the new token
         */
        public Mono<ResponseEntity<TokenResponseDto>> refreshLogin(RefreshRequestDto request) {
                return webClient.post()
                                .uri("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode()))));
                                        }

                                        Mono<TokenResponseDto> bodyMono = response.bodyToMono(TokenResponseDto.class);

                                        return bodyMono.map(tokenDto -> {
                                                ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

                                                // Forward all Set-Cookie headers (including refresh_token) to the
                                                // client
                                                response.headers().asHttpHeaders()
                                                                .getOrDefault(HttpHeaders.SET_COOKIE,
                                                                                Collections.emptyList())
                                                                .forEach(cookie -> builder.header(
                                                                                HttpHeaders.SET_COOKIE, cookie));

                                                return builder.body(tokenDto);
                                        });
                                });
        }

        /**
         * Sets a new password for the user by sending the new password to the set
         * password endpoint.
         * 
         * @param token             The authorization token
         * @param passwordUpdateDto The PasswordUpdateDto containing the old and new
         *                          passwords
         * @return A Mono<ResponseEntity<MessageResponseDto>> containing the password
         *         update
         *         response (e.g., token or status message)
         */
        public Mono<ResponseEntity<MessageResponseDto>> updatePassword(String token,
                        PasswordUpdateDto passwordUpdateDto) {

                return webClient.put()
                                .uri("/auth/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .bodyValue(passwordUpdateDto)
                                .retrieve()
                                .onStatus(status -> status.value() >= 400,
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode())))))
                                .toEntity(MessageResponseDto.class); // expect the response as a ResponseEntity<String>
                                                                     // (e.g., a
                // token or message);
        }

        /**
         * Soft deletes a user by sending a delete request to the user deletion
         * endpoint.
         * 
         * @param token  The authorization token
         * @param userId The ID of the user to delete
         * @return A Mono<ResponseEntity<MessageResponseDto>> containing the deletion
         *         response (e.g., token or status message)
         */
        public Mono<ResponseEntity<Map<String, String>>> deleteGlobalUser(String token, Long userId) {
                return webClient.delete()
                                .uri("/users/" + userId)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .onStatus(status -> status.value() >= 400,
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode())))))
                                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                                })
                                .map(body -> ResponseEntity.ok(body));
        }

        /**
         * Permanently deletes a user by sending a delete request to the permanent
         * delete
         * user endpoint.
         * 
         * @param token  The authorization token
         * @param userId The ID of the user to delete permanently
         * @return A Mono<ResponseEntity<MessageResponseDto>> containing the permanent
         *         deletion response (e.g., token or status message)
         */
        public Mono<ResponseEntity<Map<String, String>>> deleteGlobalUserPermanent(String token, Long userId) {
                return webClient.delete()
                                .uri("/users/" + userId + "/permanent")
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .onStatus(status -> status.value() >= 400,
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppException(error.message(),
                                                                                                HttpStatus.resolve(
                                                                                                                response.rawStatusCode())))))
                                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                                })
                                .map(body -> ResponseEntity.ok(body));
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

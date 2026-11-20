package ch.sectioninformatique.template.auth;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import ch.sectioninformatique.template.app.errors.ErrorDto;
import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.app.exceptions.AppMessageKeyException;
import ch.sectioninformatique.template.auth.AuthExceptions.AuthCodeNotFoundException;
import ch.sectioninformatique.template.auth.AuthExceptions.InvalidCredentialsException;
import ch.sectioninformatique.template.auth.AuthExceptions.PasswordUpdateFailedException;
import ch.sectioninformatique.template.auth.AuthExceptions.RegistrationFailedException;
import ch.sectioninformatique.template.auth.AuthExceptions.LoginAlreadyExistsException;
import ch.sectioninformatique.template.auth.AuthExceptions.UserNotFoundException;
import ch.sectioninformatique.template.security.MainRoleEnum;
import ch.sectioninformatique.template.security.SecurityExceptions.InvalidRefreshTokenException;
import ch.sectioninformatique.template.user.UserExceptions.UserDeletionException;
import ch.sectioninformatique.template.user.UserExceptions.UserUpdateException;
import ch.sectioninformatique.template.user.UserDto;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriBuilder;

/**
 * Client service for authentication operations.
 * This service provides methods to interact with the spring-auth application's
 * endpoints,
 * including login and registration functionalities.
 * It uses WebClient to perform HTTP requests and handle responses reactively.
 * Error responses are propagated as message keys so the API can localize messages.
 */
@Service
@Validated
@SuppressWarnings("null")
public class AuthClient {

        /** WebClient instance for making HTTP requests */
        private final WebClient webClient;

        /** Url for spring-auth azure login endpoint */
        @Value("${AZURE_LOGIN_URL}")
        private String azureLoginUrl;

        /** Url for spring-auth callback after oauth2 login success */
        @Value("${AFTER_OAUTH2_LOGIN_URL}")
        private String afterOauth2LoginUrl;

        /** Constructor to initialize the WebClient */
        public AuthClient(@Value("${SPRING_AUTH_URL}") String authUrl) {
                this.webClient = WebClient.create(authUrl);
        }

        // Logger for debugging and monitoring the authentication flow.
        private static final Logger log = LoggerFactory.getLogger(AuthClient.class);

        /**
         * Helper method to build URIs with an optional "lang" query parameter based on the current request context.
         * @param path the path to append to the base URI for the authentication provider
         * @return a Function that takes a UriBuilder and returns a URI with the optional "lang" parameter if it exists in the current request
         */
        private Function<UriBuilder, URI> uriWithOptionalLang(String path) {
                return uriBuilder -> {
                        UriBuilder builder = uriBuilder.path(path);
                        String lang = getCurrentLangParameter();
                        if (lang != null && !lang.isBlank()) {
                                builder.queryParam("lang", lang);
                        }
                        return builder.build();
                };
        }

        /**
         * Retrieves the "lang" query parameter from the current HTTP request, if available.
         * @return the "lang" parameter value or null if not present
         */
        private String getCurrentLangParameter() {
                RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
                if (attributes instanceof ServletRequestAttributes servletAttributes) {
                        return servletAttributes.getRequest().getParameter("lang");
                }
                return null;
        }

        /**
         * Performs classic user login by sending credentials to the authentication provider.
         * 
         * @param credentialsDto The CredentialsDto containing user login data
         * @return A Mono<ResponseEntity<UserDto>> containing the authentication response
         *         (e.g., token or status message)
         */
        public Mono<ResponseEntity<UserDto>> login(@Valid CredentialsDto credentialsDto) {

                return webClient.post()
                                .uri(uriWithOptionalLang("/auth/login")) // classic login endpoint path in authentication provider
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(credentialsDto)
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> {
                                                                        HttpStatusCode status = response.statusCode();
                                                                        if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) {
                                                                                return Mono.error(new UserNotFoundException());
                                                                        }
                                                                        return Mono.error(new InvalidCredentialsException());
                                                                });
                                        }

                                        // Extract response body
                                        Mono<UserDto> bodyMono = response.bodyToMono(UserDto.class);

                                        // Extract Set-Cookie header and include it in the response if present
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
         * Build the URI for the spring-auth OAuth2 login endpoint with Azure,
         * including a callback url in the query parameter.
         * 
         * @return The built URI for spring-auth OAuth2 login endpoint with Azure.
         */
        public URI buildAzureLoginUri() {
                var builder = org.springframework.web.util.UriComponentsBuilder.fromUriString(azureLoginUrl);

                String lang = getCurrentLangParameter();
                if (lang != null && !lang.isBlank()) {
                        builder.queryParam("lang", lang);
                }

                // After successful login, spring-auth has to redirect to our callback endpoint
                builder.queryParam("redirectUrl", afterOauth2LoginUrl);

                return builder.build().toUri();
        }

        /**
         * Performs user registration by sending user details to the authentication provider.
         * 
         * @param user The SignUpDto containing user registration data
         * @return A Mono<ResponseEntity<UserDto>> containing the registration response
         *         (e.g., token or status message)
         */
        public Mono<ResponseEntity<UserDto>> register(String token, RegisterDto user) {

                return webClient.post()
                                .uri(uriWithOptionalLang("/users")) // the registration endpoint path in authentication provider
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(user)
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> {
                                                                        if (response.statusCode().isSameCodeAs(
                                                                                        HttpStatus.CONFLICT)) {
                                                                                return Mono.error(
                                                                                                new LoginAlreadyExistsException(
                                                                                                                error.message()));
                                                                        }
                                                                        return Mono.error(
                                                                                        new RegistrationFailedException(
                                                                                                        error.message()));
                                                                });
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
         * Call the authentication provider to refresh the access token using a refresh
         * token
         * 
         * @param refreshToken The refresh token cookie value
         * @return A Mono<ResponseEntity<TokenResponseDto>> containing the new access token
         */
        public Mono<ResponseEntity<TokenResponseDto>> refreshLogin(String refreshToken) {
                return webClient.post()
                                .uri(uriWithOptionalLang("/auth/refresh")) // the refresh token endpoint path in authentication provider
                                .cookie("refresh_token", refreshToken)
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new InvalidRefreshTokenException(
                                                                                                error.message())));
                                        }

                                        Mono<TokenResponseDto> bodyMono = response.bodyToMono(TokenResponseDto.class);

                                        return bodyMono.map(tokenDto -> {
                                                ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

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
         * Updates user's password by sending the new password to the authentication
         * provider.
         * 
         * @param token             The access token
         * @param passwordUpdateDto The PasswordUpdateDto containing the old and new
         *                          passwords
         * @return A Mono<ResponseEntity<Void>> completing with the bodiless
         *         response (204 No Content) of the authentication provider
         */
        public Mono<ResponseEntity<Void>> updatePassword(String token,
                        PasswordUpdateDto passwordUpdateDto) {
                return webClient.put()
                                .uri(uriWithOptionalLang("/users/me/password")) // own password update endpoint in authentication provider
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .bodyValue(passwordUpdateDto)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError,
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new PasswordUpdateFailedException(
                                                                                                error.message()))))
                                .toBodilessEntity();
        }

        /**
         * Logs out the authenticated user by sending a logout request to the
         * authentication provider.
         * Extracts and retransmits the Set-Cookie header containing the expired refresh
         * token.
         * 
         * @param token The access token
         * @return A Mono<ResponseEntity<Map<String, String>>> containing the logout
         *         response with Set-Cookie header
         */
        public Mono<ResponseEntity<Map<String, String>>> logout(String token) {
                return webClient.post()
                                .uri(uriWithOptionalLang("/auth/logout")) // the logout endpoint path in authentication provider
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .exchangeToMono(response -> {
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(new AppMessageKeyException(
                                                                                HttpStatus.resolve(response
                                                                                                .statusCode()
                                                                                                .value()),
                                                                                error.message())));
                                        }

                                        // Extract response body
                                        Mono<Map<String, String>> bodyMono = response.bodyToMono(
                                                        new ParameterizedTypeReference<Map<String, String>>() {
                                                        });

                                        return bodyMono.map(body -> {
                                                ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

                                                // Extract refresh_token cookie (expired) directly in the lambda
                                                response.headers().asHttpHeaders()
                                                                .getOrDefault(HttpHeaders.SET_COOKIE,
                                                                                Collections.emptyList())
                                                                .stream()
                                                                .filter(cookie -> cookie.startsWith("refresh_token="))
                                                                .findFirst()
                                                                .ifPresent(cookie -> builder.header(
                                                                                HttpHeaders.SET_COOKIE, cookie));
                                                return builder.body(body);
                                        });
                                });
        }

        /**
         * deletes a user by sending a delete request to the authentication
         * provider.
         * 
         * @param token  The access token
         * @param userId The ID of the user to delete
         * @param permanent control the type of delete (soft = false, hard = true)
         * @return A Mono<ResponseEntity<Void>> completing with the bodiless
         *         response (204 No Content) of the authentication provider
         */
        public Mono<ResponseEntity<Void>> deleteGlobalUser(String token, Long userId, boolean permanent) {
                return webClient.delete()
                                .uri(uriWithOptionalLang("/users/" + userId + "?permanent=" + permanent))
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .onStatus(status -> status.value() >= 400,
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(new UserDeletionException(
                                                                                error.message()))))
                                .toBodilessEntity();
        }

        /**
         * Restores a soft-deleted user by sending a restore request to the
         * authentication provider.
         * Restoring a global user that was not soft-deleted is not an error:
         * spring-auth answers 404 Not Found when no soft-deleted user has this id
         * (e.g. the user was only deleted locally), which is treated here as
         * "nothing to restore globally".
         *
         * @param token     The access token
         * @param userId The id of the user to restore
         * @return A Mono<ResponseEntity<Void>> completing once the global user is
         *         restored, or was not soft-deleted
         * @throws UserUpdateException if the authentication service returns
         *                             another error status
         */
        public Mono<ResponseEntity<Void>> restoreGlobalUser(String token, Long userId) {
                return webClient.post()
                                .uri(uriWithOptionalLang("/users/" + userId + "/restore")) // restore user endpoint path in authentication provider
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .onStatus(status -> status.value() >= 400
                                                && !status.isSameCodeAs(HttpStatus.NOT_FOUND),
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(new UserUpdateException(
                                                                                error.message()))))
                                .toBodilessEntity()
                                // Not soft-deleted globally: nothing to restore on spring-auth side
                                .onErrorResume(WebClientResponseException.NotFound.class,
                                                e -> Mono.just(ResponseEntity.noContent().build()));
        }

        /**
         * Changes the global main role of a user by sending a PUT request to the
         * authentication service ({@code PUT /users/{id}/role}).
         * spring-auth owns the main roles: it checks that the actor may manage both
         * the current and the new role, and refuses to change the actor's own role.
         *
         * @param token   the authorization token (Bearer token) to authenticate the
         *                request
         * @param userId  the ID of the user whose main role changes
         * @param newRole the new main role of the user
         * @return a Mono containing the ResponseEntity with the updated global user
         * @throws AppException if the authentication service returns an error status
         *                      (4xx or 5xx)
         */
        public Mono<ResponseEntity<UserDto>> changeMainRole(String token, Long userId, MainRoleEnum newRole) {
                return webClient.put()
                                .uri(uriWithOptionalLang("/users/" + userId + "/role"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .bodyValue(Map.of("role", newRole.name()))
                                .retrieve()
                                // Convert error responses (4xx and 5xx) into AppException
                                .onStatus(status -> status.value() >= 400,
                                                response -> response.bodyToMono(ErrorDto.class)
                                                                .flatMap(error -> Mono.error(
                                                                                new AppMessageKeyException(
                                                                                                HttpStatus.resolve(
                                                                                                                response.statusCode()
                                                                                                                                .value()),
                                                                                                error.message()))))
                                .toEntity(UserDto.class);
        }

        /**
         * Retrieves the access and refresh tokens using the provided temporary authentication code.
         * 
         * @param authCodeDto The DTO containing the authentication code.
         * @return A Mono emitting the ResponseEntity with the user details, including tokens
         */
        public Mono<ResponseEntity<UserDto>> getTokenWithAuthCode(AuthCodeDto authCodeDto){
                return webClient.post()
                .uri(uriWithOptionalLang("/oauth2/token"))
                .bodyValue(authCodeDto)
                .exchangeToMono(response ->{
                        if(response.statusCode().isError()){
                                return response.bodyToMono(ErrorDto.class)
                                .flatMap(error ->{
                                        HttpStatusCode status = response.statusCode();
                                        if(status.isSameCodeAs(HttpStatus.NOT_FOUND)){
                                                return Mono.error(new AuthCodeNotFoundException());
                                        }
                                        return Mono.error(new RuntimeException(error.message()));

                                });
                        }

                        //Extract the body
                        Mono<UserDto> bodyMono = response.bodyToMono(UserDto.class);
                        return bodyMono.map(userDto ->{
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
 * Method for updating users
 * @param token the access token
 * @param userId the user's email
 * @param userDto the user's DTO
 */

public Mono<ResponseEntity<String>> updateUser(String token, Long userId, UserDto userDto){
        return webClient.put()
        .uri(uriWithOptionalLang("/users/" + userId))
        .header(HttpHeaders.AUTHORIZATION, token)
        .bodyValue(userDto)
        .retrieve()
        .onStatus(status -> status.value() >= 400,
                response -> response.bodyToMono(ErrorDto.class)
        .flatMap(error -> Mono.error(new UserUpdateException(error.message()))))

        .toEntity(String.class);
}

}
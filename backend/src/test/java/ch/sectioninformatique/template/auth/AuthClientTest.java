package ch.sectioninformatique.template.auth;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sun.net.httpserver.HttpServer;

import ch.sectioninformatique.template.auth.AuthExceptions.AuthCodeNotFoundException;
import ch.sectioninformatique.template.auth.AuthExceptions.LoginAlreadyExistsException;
import ch.sectioninformatique.template.auth.AuthExceptions.OAuth2AuthenticationException;
import ch.sectioninformatique.template.user.UserExceptions.UserDeletionException;
import ch.sectioninformatique.template.user.UserExceptions.UserUpdateException;

/**
 * Tests the requests {@link AuthClient} sends to spring-auth, against a local
 * HTTP server standing in for spring-auth. Controller tests mock AuthClient, so
 * they can't catch a wrong URL.
 */
public class AuthClientTest {

    private HttpServer server;

    /** Method and URI (raw, as sent on the wire) of the last request received */
    private final AtomicReference<String> lastMethod = new AtomicReference<>();
    private final AtomicReference<URI> lastUri = new AtomicReference<>();

    /** Next answer of the fake spring-auth: by default 204 No Content (successful delete) */
    private final AtomicInteger responseStatus = new AtomicInteger(204);
    private final AtomicReference<String> responseBody = new AtomicReference<>();

    private AuthClient authClient;

    private void respond(int status, String body) {
        responseStatus.set(status);
        responseBody.set(body);
    }

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            lastMethod.set(exchange.getRequestMethod());
            lastUri.set(exchange.getRequestURI());
            String body = responseBody.get();
            if (body == null) {
                exchange.sendResponseHeaders(responseStatus.get(), -1);
            } else {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(responseStatus.get(), bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
        authClient = new AuthClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * The "permanent" flag must be sent as a query parameter, not encoded in the
     * path (which spring-auth would read as an invalid user id "9?permanent=...").
     */
    @Test
    void deleteGlobalUser_shouldSendPermanentAsQueryParameter() {
        authClient.deleteGlobalUser("Bearer token", 9L, true).block();

        assertEquals("DELETE", lastMethod.get());
        assertEquals("/users/9", lastUri.get().getRawPath());
        assertEquals("permanent=true", lastUri.get().getRawQuery());
    }

    /**
     * The "lang" parameter of the current request is forwarded next to the
     * other query parameters.
     */
    @Test
    void deleteGlobalUser_withLangParameter_shouldForwardLangAsQueryParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("lang", "en");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        authClient.deleteGlobalUser("Bearer token", 9L, false).block();

        assertEquals("/users/9", lastUri.get().getRawPath());
        assertEquals("permanent=false&lang=en", lastUri.get().getRawQuery());
    }

    // ==================== Error mapping ====================

    /**
     * A client error of spring-auth keeps its status (here 404), not a generic 400.
     */
    @Test
    void deleteGlobalUser_whenSpringAuthAnswers404_shouldKeep404() {
        respond(404, "{\"message\":\"User not found: 9\"}");

        UserDeletionException e = assertThrows(UserDeletionException.class,
                () -> authClient.deleteGlobalUser("Bearer token", 9L, false).block());

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertArrayEquals(new Object[] { "User not found: 9" }, e.getMessageArgs());
    }

    /**
     * An error without body is still an error, not a successful deletion.
     */
    @Test
    void deleteGlobalUser_whenSpringAuthAnswersErrorWithoutBody_shouldFail() {
        respond(403, null);

        UserDeletionException e = assertThrows(UserDeletionException.class,
                () -> authClient.deleteGlobalUser("Bearer token", 9L, false).block());

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    /**
     * A server error of spring-auth is answered 502 Bad Gateway.
     */
    @Test
    void updateUser_whenSpringAuthAnswers500_shouldAnswer502() {
        respond(500, "{\"message\":\"boom\"}");

        UserUpdateException e = assertThrows(UserUpdateException.class,
                () -> authClient.updateUser("Bearer token", 9L, Map.of("login", "x@test.com")).block());

        assertEquals(HttpStatus.BAD_GATEWAY, e.getStatus());
    }

    /**
     * A refused auth code exchange is an OAuth2 authentication failure (401),
     * not an unexpected 500.
     */
    @Test
    void getTokenWithAuthCode_whenSpringAuthAnswers400_shouldThrowOAuth2Failure() {
        respond(400, "{\"message\":\"Validation failed\"}");

        OAuth2AuthenticationException e = assertThrows(OAuth2AuthenticationException.class,
                () -> authClient.getTokenWithAuthCode(AuthCodeDto.builder().code("bogus").id(2L).build()).block());

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    /**
     * An unknown or expired auth code is answered 404.
     */
    @Test
    void getTokenWithAuthCode_whenSpringAuthAnswers404_shouldThrowAuthCodeNotFound() {
        respond(404, null);

        assertThrows(AuthCodeNotFoundException.class,
                () -> authClient.getTokenWithAuthCode(AuthCodeDto.builder().code("old").id(2L).build()).block());
    }

    /**
     * A login already used in spring-auth (409) is a conflict, not a bad request.
     */
    @Test
    void register_whenSpringAuthAnswers409_shouldThrowLoginAlreadyExistsWith409() {
        respond(409, "{\"message\":\"Login already exists\"}");

        LoginAlreadyExistsException e = assertThrows(LoginAlreadyExistsException.class,
                () -> authClient.register("Bearer token", new RegisterDto("A", "B", "a@b.ch",
                        "Secure123@Pass".toCharArray(), null, null)).block());

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }
}

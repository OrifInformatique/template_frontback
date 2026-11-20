package ch.sectioninformatique.template.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sun.net.httpserver.HttpServer;

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

    private AuthClient authClient;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            lastMethod.set(exchange.getRequestMethod());
            lastUri.set(exchange.getRequestURI());
            // spring-auth answers 204 No Content to a successful delete
            exchange.sendResponseHeaders(204, -1);
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
}

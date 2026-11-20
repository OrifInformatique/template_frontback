package ch.sectioninformatique.template.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Access to the access token of the authenticated request, to call spring-auth
 * on behalf of the user.
 *
 * {@link UserAuthenticationProvider#validateToken} keeps the validated JWT as the
 * credentials of the authentication, so controllers don't need to read the
 * Authorization header themselves.
 */
public final class BearerToken {

    private BearerToken() {
    }

    /**
     * @return the "Authorization" header value of the authenticated request
     *         ("Bearer " followed by the access token)
     * @throws IllegalStateException if the request is not authenticated by a token
     */
    public static String current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getCredentials() instanceof String token)) {
            throw new IllegalStateException("No authenticated access token in the security context");
        }
        return "Bearer " + token;
    }
}

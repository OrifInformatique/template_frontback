package ch.sectioninformatique.template.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

/**
 * Security configuration class for the application.
 * This class configures Spring Security settings including:
 * - Authentication and authorization rules
 * - CORS configuration
 * - JWT filter integration
 * - Session management
 * - Exception handling
 * 
 * The configuration ensures:
 * - Secure endpoints with appropriate authorization
 * - Cross-origin request handling
 * - Session management
 * - Custom authentication failure handling
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    /**
     * Entry point for handling authentication failures.
     * This component:
     * - Provides custom responses for unauthenticated requests
     * - Formats error messages in JSON
     * - Sets appropriate HTTP status codes
     */
    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;

    /**
     * Entry point for denied access failures.
     */
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * Filter for JWT token authentication.
     * This component:
     * - Validates JWT tokens in requests
     * - Extracts user information from tokens
     * - Sets up authentication context
     */

    @Autowired
    private @Lazy JwtAuthFilter jwtAuthFilter;

    /**
     * Configures the security filter chain with all necessary security settings.
     * This method:
     * - Sets up exception handling with custom entry point
     * - Configures JWT authentication filter
     * - Disables CSRF protection (not needed for stateless API)
     * - Sets session management policy to ALWAYS
     * - Keeps the security context per request (never in the session)
     * - Configures CORS with allowed origins and methods
     * - Defines HTTP request authorization rules
     *
     * @param http The HttpSecurity object to configure
     * @return The configured SecurityFilterChain
     * @throws Exception if security configuration fails
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring SecurityFilterChain");
        http
                .exceptionHandling(customizer -> {
                    log.debug("Configuring exception handling with UserAuthenticationEntryPoint");
                    customizer
                            .authenticationEntryPoint(userAuthenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler);
                })
                .addFilterBefore(jwtAuthFilter, BasicAuthenticationFilter.class)
                .csrf(csrf -> {
                    log.debug("Disabling CSRF protection");
                    csrf.disable();
                })
                .sessionManagement(customizer -> {
                    log.debug("Setting session creation policy to ALWAYS");
                    // OAuth2 login flow relies on a session to store auth requests.
                    customizer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
                })
                .securityContext(securityContext -> {
                    // Authentication comes from the JWT of each request only: keep the
                    // security context for the current request, never in the session,
                    // or the JSESSIONID cookie alone would authenticate later requests.
                    securityContext.securityContextRepository(new RequestAttributeSecurityContextRepository());
                })
                .cors(cors -> {
                    log.debug("Configuring CORS");
                    cors.configurationSource(request -> {
                        var corsConfig = new CorsConfiguration();
                        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4000", "http://localhost:8080"));
                        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        corsConfig.setAllowedHeaders(Arrays.asList("*"));
                        corsConfig.setAllowCredentials(true);
                        return corsConfig;
                    });
                })
                .authorizeHttpRequests(requests -> {
                    log.debug("Configuring HTTP request authorization rules");
                    requests
                            .requestMatchers("/error").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                            // Azure login (OAuth2Controller): browser redirects and the
                            // frontend taking the result, before any access token exists
                            .requestMatchers(HttpMethod.GET, "/oauth2/login/azure").permitAll()
                            .requestMatchers(HttpMethod.GET, "/oauth2/callback").permitAll()
                            .requestMatchers(HttpMethod.POST, "/oauth2/token").permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .anyRequest().authenticated();
                    log.debug("HTTP request authorization rules configured");
                });

        return http.build();
    }
}
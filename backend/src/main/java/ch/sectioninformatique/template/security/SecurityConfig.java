package ch.sectioninformatique.template.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import ch.sectioninformatique.template.jwt.JwtAuthFilter;
import ch.sectioninformatique.template.user.UserAuthenticationEntryPoint;
import ch.sectioninformatique.template.user.UserAuthenticationProvider;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
  private final UserAuthenticationProvider userAuthenticationProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Exception handling for authentication failures
        .exceptionHandling(customizer -> customizer
            .authenticationEntryPoint(userAuthenticationEntryPoint))
        // Add JWT filter before Basic Authentication
        .addFilterBefore(new JwtAuthFilter(userAuthenticationProvider),
            BasicAuthenticationFilter.class)
        // Disable CSRF (not needed for stateless APIs)
        .csrf(AbstractHttpConfigurer::disable)
        // Set session management to stateless (no sessions)
        .sessionManagement(customizer -> customizer
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        // Configure authorization rules
        .authorizeHttpRequests((requests) -> requests
            // Allow unauthenticated access to login, register, and OAuth2 endpoints
            .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/oauth2/**", "/login/oauth2/**",
                "/auth/success",
                "/oauth2/success")
            .permitAll()
            // Require authentication for all other requests
            .anyRequest().authenticated())
        // Configure OAuth2 login
        // `<base_URL>/oauth2/authorization/<registrationId>` is used by `.oauth2Login`
        // ^^^^ This is the "redirect URI" ^^^^
        .oauth2Login(oauth2 -> oauth2
            // Redirect to a success URL after OAuth2 login
            .defaultSuccessUrl("/oauth2/success", true));

    return http.build();
  }
}

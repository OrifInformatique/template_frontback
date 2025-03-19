package ch.sectioninformatique.template.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;

import ch.sectioninformatique.template.jwt.JwtAuthFilter;
import ch.sectioninformatique.template.user.UserAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring SecurityFilterChain");
        http
            .exceptionHandling(customizer -> {
                log.debug("Configuring exception handling with UserAuthenticationEntryPoint");
                customizer.authenticationEntryPoint(userAuthenticationEntryPoint);
            })
            .addFilterBefore(jwtAuthFilter, BasicAuthenticationFilter.class)
            .csrf(csrf -> {
                log.debug("Disabling CSRF protection");
                csrf.disable();
            })
            .sessionManagement(customizer -> {
                log.debug("Setting session creation policy to ALWAYS");
                customizer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
            })
            .cors(cors -> {
                log.debug("Configuring CORS");
                cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4000")); 
                    corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(Arrays.asList("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                });
            })
            .oauth2Login(oauth2 -> {
                log.debug("Configuring OAuth2 login");
                oauth2
                    .defaultSuccessUrl("/oauth2/success", true)
                    .failureUrl("/oauth2/error")
                    .userInfoEndpoint(userInfo -> 
                        userInfo.userService(oauth2UserService())
                    )
                    .successHandler((request, response, authentication) -> {
                        log.debug("OAuth2 authentication successful: {}", authentication);
                        response.sendRedirect("/oauth2/success");
                    });
            })
            .authorizeHttpRequests(requests -> {
                log.debug("Configuring HTTP request authorization rules");
                requests
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/oauth2/authorization/**").permitAll()
                    .requestMatchers("/oauth2/success").authenticated()
                    .requestMatchers("/oauth2/error").permitAll()
                    .requestMatchers("/login/oauth2/code/**").permitAll()
                    .anyRequest().authenticated();
                log.debug("HTTP request authorization rules configured");
            });
            
        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            log.debug("OAuth2 user loaded: {}", user.getAttributes());
            return user;
        };
    }
}

package ch.sectioninformatique.template.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Links accessible to unregistered users
                        .requestMatchers("/oauth2/**", "/login", "/auth/**").permitAll()
                        .anyRequest().permitAll())
                // Login form using spring web security
                // TODO: use react for the form instead but keep the logic
                // in the backend
                .formLogin(form -> form
                        // custom login redirection
                        .defaultSuccessUrl("/auth/success", true))
                .oauth2Login(oauth2 -> oauth2
                        // oauth2 login redirection
                        .defaultSuccessUrl("/oauth2/success", true));

        return http.build();
    }
}

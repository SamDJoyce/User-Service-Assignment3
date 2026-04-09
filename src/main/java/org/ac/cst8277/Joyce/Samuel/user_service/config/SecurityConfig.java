package org.ac.cst8277.Joyce.Samuel.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
    														OAuthHandler handler) {
        return http
        	.csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
            	// Permit access to validate and new user without login
                .pathMatchers("/users/validate").permitAll()
                .pathMatchers("/users/new-user").permitAll()
                .pathMatchers("/oauth2/**", "/login/**").permitAll()
                // Everything else needs login
                .anyExchange().authenticated()
            )
            .oauth2Login(oauth -> oauth.authenticationSuccessHandler(handler))
            .build();
    }
}
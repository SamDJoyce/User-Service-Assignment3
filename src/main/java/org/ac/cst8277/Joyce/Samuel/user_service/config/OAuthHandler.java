package org.ac.cst8277.Joyce.Samuel.user_service.config;

import java.net.URI;

import org.ac.cst8277.Joyce.Samuel.user_service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class OAuthHandler implements ServerAuthenticationSuccessHandler {

    private final UserService userService;

    public OAuthHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String username = oauthUser.getAttribute("login");
        String email = oauthUser.getAttribute("email");

        if (email == null) {
            email = username + "@github.com";
        }

        return userService.oAuthLogin(username, email)
        		.flatMap(token -> {
        			// Authorized
                    exchange.getExchange().getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                    exchange.getExchange().getResponse().getHeaders().setLocation(
                            java.net.URI.create("/token?value=" + token)
                    );
                    return exchange.getExchange().getResponse().setComplete();
                })
                .onErrorResume(e -> {
                    // Unauthorized
                    exchange.getExchange().getResponse().setStatusCode(
                            org.springframework.http.HttpStatus.UNAUTHORIZED
                    );
                    return exchange.getExchange().getResponse().setComplete();
                });
    }
}
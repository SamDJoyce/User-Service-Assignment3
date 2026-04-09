package org.ac.cst8277.Joyce.Samuel.user_service.config;

import java.nio.charset.StandardCharsets;

import org.ac.cst8277.Joyce.Samuel.user_service.UserService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange,
                                              Authentication authentication) {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2User user = oauthToken.getPrincipal();

        // GitHub username
        String githubUsername = user.getAttribute("login");

        // Generate a token
        String token = userService.handleOAuthLogin(githubUsername);

        // Return token in response
        ServerHttpResponse response = exchange.getExchange().getResponse();

        response.getHeaders().set("Content-Type", "application/json");

        String body = "{\"token\": \"" + token + "\"}";

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
package com.livelikelocal.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class UserInfoFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Skip filter for public routes
        if (isPublicRoute(path)) {
            return chain.filter(exchange);
        }

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(jwt -> {
                    String userId = jwt.getToken().getSubject();
                    String userRole = jwt.getToken().getClaimAsString("role");
                    String userType = jwt.getToken().getClaimAsString("userType");
                    List<String> authorities = jwt.getToken().getClaimAsStringList("authorities");

                    String authoritiesHeaderValue = (authorities != null)
                            ? String.join(",", authorities)
                            : "";

                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", userRole != null ? userRole : "")
                            .header("X-User-Type", userType != null ? userType : "")
                            .header("X-User-Authorities", authoritiesHeaderValue)
                            .build();

                    return exchange.mutate().request(request).build();
                })
                .flatMap(chain::filter)
                .switchIfEmpty(chain.filter(exchange));
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/api/auth") ||
               path.startsWith("/api/guides") ||
               path.startsWith("/api/users") ||
               path.startsWith("/api/cities") ||
               path.startsWith("/api/languages") ||
               path.startsWith("/api/skills") ||
               path.startsWith("/api/matching") ||
               path.startsWith("/api/reviews") ||
               path.startsWith("/actuator");
    }
}

package com.livelikelocal.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class RequestLoggingFilter {

    @Bean
    public GlobalFilter logFilter() {
        return (exchange, chain) -> {
            ServerWebExchange ex = exchange;
            String path = ex.getRequest().getURI().getPath();
            String method = ex.getRequest().getMethod() != null ? ex.getRequest().getMethod().name() : "NA";
            System.out.println("[GATEWAY] " + method + " " + path);
            return chain.filter(exchange).then(Mono.empty());
        };
    }
}
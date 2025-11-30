package lk.tech.tgcontrollerbot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();

        logRequest(req);

        return chain.filter(exchange)
                .doOnSuccess(done -> {
                    ServerHttpResponse res = exchange.getResponse();
                    logResponse(res);
                });
    }

    private void logRequest(ServerHttpRequest request) {
        log.info("➡ {} {}", request.getMethod(), request.getURI());

        HttpHeaders headers = request.getHeaders();
        headers.forEach((k, v) -> log.info("   ↳ Req Header: {} = {}", k, v));
    }

    private void logResponse(ServerHttpResponse response) {
        log.info("⬅ Response: {}", response.getStatusCode());

        HttpHeaders headers = response.getHeaders();
        headers.forEach((k, v) -> log.info("   ↳ Res Header: {} = {}", k, v));
    }
}

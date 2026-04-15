package com.erzbir.injector.halo.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathMatcherFactoryTest {

    @Test
    void shouldMatchPublicHtmlGetRequest() {
        ServerWebExchangeMatcher matcher = PathMatcherFactory.create();
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/posts/1")
                        .accept(MediaType.TEXT_HTML)
                        .build()
        );

        ServerWebExchangeMatcher.MatchResult result = matcher.matches(exchange).block();

        assertTrue(result != null && result.isMatch());
    }

    @Test
    void shouldNotMatchExcludedPath() {
        ServerWebExchangeMatcher matcher = PathMatcherFactory.create();
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/console/dashboard")
                        .accept(MediaType.TEXT_HTML)
                        .build()
        );

        ServerWebExchangeMatcher.MatchResult result = matcher.matches(exchange).block();

        assertFalse(result != null && result.isMatch());
    }

    @Test
    void shouldNotMatchNonHtmlRequest() {
        ServerWebExchangeMatcher matcher = PathMatcherFactory.create();
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/posts/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .build()
        );

        ServerWebExchangeMatcher.MatchResult result = matcher.matches(exchange).block();

        assertFalse(result != null && result.isMatch());
    }

    @Test
    void shouldNotMatchNonGetRequest() {
        ServerWebExchangeMatcher matcher = PathMatcherFactory.create();
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/posts/1")
                        .accept(MediaType.TEXT_HTML)
                        .build()
        );

        ServerWebExchangeMatcher.MatchResult result = matcher.matches(exchange).block();

        assertFalse(result != null && result.isMatch());
    }
}

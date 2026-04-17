package com.erzbir.injector.halo.filter;

import com.erzbir.injector.halo.core.InjectHelper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InjectorWebFilterTest {

    private static ServerWebExchange arg(java.util.function.Predicate<ServerWebExchange> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }

    @Test
    void shouldPassThroughWhenRequestNotMatchedByPathMatcher() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        InjectorWebFilter filter = new InjectorWebFilter(injectHelper);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/console/dashboard")
                        .accept(org.springframework.http.MediaType.TEXT_HTML)
                        .build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldDecorateResponseWhenRequestMatchedByPathMatcher() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        InjectorWebFilter filter = new InjectorWebFilter(injectHelper);
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/posts/1")
                        .accept(org.springframework.http.MediaType.TEXT_HTML)
                        .build()
        );
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(arg(arg -> arg.getResponse() instanceof InjectorResponseDecorator
                && arg != exchange
                && arg.getRequest().getPath().value().equals("/posts/1")));
    }

    @Test
    void shouldDecorateResponseWhenRulesExist() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        InjectorWebFilter filter = new InjectorWebFilter(injectHelper);
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/posts/1")
                        .accept(org.springframework.http.MediaType.TEXT_HTML)
                        .build()
        );
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(arg(arg -> arg.getResponse() instanceof InjectorResponseDecorator
                && arg != exchange
                && arg.getRequest().getPath().value().equals("/posts/1")));
    }

    @Test
    void shouldDecorateResponseEvenWhenStatusIsNotOk() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        InjectorWebFilter filter = new InjectorWebFilter(injectHelper);
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/posts/1")
                        .accept(org.springframework.http.MediaType.TEXT_HTML)
                        .build()
        );
        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(arg(arg -> arg.getResponse() instanceof InjectorResponseDecorator
                && arg != exchange
                && arg.getRequest().getPath().value().equals("/posts/1")));
    }
}

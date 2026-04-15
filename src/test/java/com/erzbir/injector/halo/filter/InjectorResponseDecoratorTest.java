package com.erzbir.injector.halo.filter;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InjectorResponseDecoratorTest {

    @Test
    void shouldBypassDispatcherForNonHtmlResponse() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/1").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("{\"ok\":true}".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("{\"ok\":true}", body);
    }

    @Test
    void shouldUseDispatcherForHtmlResponse() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/1").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        when(dispatcher.dispatch("<html>origin</html>", "/posts/1")).thenReturn(Mono.just("<html>changed</html>"));
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("<html>origin</html>".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("<html>changed</html>", body);
        verify(dispatcher).dispatch("<html>origin</html>", "/posts/1");
    }

    @Test
    void shouldKeepOriginalHtmlWhenDispatcherFails() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/1").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        when(dispatcher.dispatch("<html>origin</html>", "/posts/1"))
                .thenReturn(Mono.error(new IllegalStateException("boom")));
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("<html>origin</html>".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("<html>origin</html>", body);
    }

    @Test
    void shouldSkipDispatcherWhenHtmlIsBlank() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/1").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("   ".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("   ", body);
        verify(dispatcher, never()).dispatch(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}

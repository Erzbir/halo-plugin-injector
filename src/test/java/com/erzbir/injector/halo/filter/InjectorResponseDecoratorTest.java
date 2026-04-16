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
import static org.mockito.Mockito.*;

class InjectorResponseDecoratorTest {

    @Test
    void shouldBypassDispatcherForNonHtmlResponse() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/non-html").build());
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
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/use-dispatcher").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        when(dispatcher.dispatch("<html>origin</html>", "/posts/use-dispatcher"))
                .thenReturn(Mono.just("<html>changed</html>"));
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("<html>origin</html>".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("<html>changed</html>", body);
        verify(dispatcher).dispatch("<html>origin</html>", "/posts/use-dispatcher");
    }

    @Test
    void shouldKeepOriginalHtmlWhenDispatcherFails() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/dispatcher-fail").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        when(dispatcher.dispatch("<html>origin</html>", "/posts/dispatcher-fail"))
                .thenReturn(Mono.error(new IllegalStateException("boom")));
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("<html>origin</html>".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("<html>origin</html>", body);
    }

    @Test
    void shouldSkipDispatcherWhenHtmlIsBlank() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/blank-html").build());
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        InjectorResponseDecorator decorator = new InjectorResponseDecorator(exchange, dispatcher);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap("   ".getBytes(StandardCharsets.UTF_8));

        decorator.writeAndFlushWith(Flux.just(Flux.just(dataBuffer))).block();

        String body = exchange.getResponse().getBodyAsString().block();
        assertEquals("   ", body);
        verify(dispatcher, never()).dispatch(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldReuseDecoratorCacheAcrossDecoratorInstances() {
        HTMLInjectDispatcher dispatcher = mock(HTMLInjectDispatcher.class);
        when(dispatcher.dispatch("<html>origin</html>", "/posts/cache-reuse"))
                .thenReturn(Mono.just("<html>changed</html>"));

        var exchange1 = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/cache-reuse").build());
        exchange1.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        InjectorResponseDecorator decorator1 = new InjectorResponseDecorator(exchange1, dispatcher);
        DataBuffer dataBuffer1 = exchange1.getResponse().bufferFactory().wrap("<html>origin</html>".getBytes(StandardCharsets.UTF_8));
        decorator1.writeAndFlushWith(Flux.just(Flux.just(dataBuffer1))).block();

        var exchange2 = MockServerWebExchange.from(MockServerHttpRequest.get("/posts/cache-reuse").build());
        exchange2.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
        InjectorResponseDecorator decorator2 = new InjectorResponseDecorator(exchange2, dispatcher);
        DataBuffer dataBuffer2 = exchange2.getResponse().bufferFactory().wrap("<html>origin</html>".getBytes(StandardCharsets.UTF_8));
        decorator2.writeAndFlushWith(Flux.just(Flux.just(dataBuffer2))).block();

        assertEquals("<html>changed</html>", exchange1.getResponse().getBodyAsString().block());
        assertEquals("<html>changed</html>", exchange2.getResponse().getBodyAsString().block());
        verify(dispatcher, times(1)).dispatch("<html>origin</html>", "/posts/cache-reuse");
    }
}

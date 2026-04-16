package com.erzbir.injector.halo.filter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
public class InjectorResponseDecorator extends ServerHttpResponseDecorator {

    private final ServerWebExchange exchange;
    private final HTMLInjectDispatcher dispatcher;

    public InjectorResponseDecorator(ServerWebExchange exchange,
                                     HTMLInjectDispatcher dispatcher) {
        super(exchange.getResponse());
        this.exchange = exchange;
        this.dispatcher = dispatcher;
    }

    @Override
    @NonNull
    public Mono<Void> writeAndFlushWith(
            @NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return writeWith(Flux.from(body).flatMapSequential(publisher -> publisher));
    }

    @Override
    @NonNull
    public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
        if (!shouldProcessResponse()) {
            return super.writeWith(body);
        }
        String path = exchange.getRequest().getPath().value();
        if (path.isBlank()) {
            return super.writeWith(body);
        }
        return DataBufferUtils.join(Flux.from(body))
                .flatMap(dataBuffer -> processBuffer(dataBuffer, getDelegate(), path))
                .flatMap(processed -> super.writeWith(Mono.just(processed)));
    }

    private Mono<DataBuffer> processBuffer(DataBuffer dataBuffer,
                                           ServerHttpResponse response,
                                           String path) {
        final String html;
        try {
            html = dataBuffer.toString(StandardCharsets.UTF_8);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
        if (html.isBlank()) {
            return Mono.just(response.bufferFactory().wrap(html.getBytes(StandardCharsets.UTF_8)));
        }
        return dispatcher.dispatch(html, path)
                .onErrorResume(e -> {
                    log.warn("Injection failed for path [{}], returning original HTML", path, e);
                    return Mono.just(html);
                })
                .map(processed -> {
                    byte[] bytes = processed.getBytes(StandardCharsets.UTF_8);
                    return response.bufferFactory().wrap(bytes);
                });
    }

    private boolean shouldProcessResponse() {
        ServerHttpResponse response = getDelegate();
        var statusCode = response.getStatusCode();
        if (statusCode != null && !statusCode.isSameCodeAs(HttpStatus.OK)) {
            return false;
        }
        MediaType contentType = response.getHeaders().getContentType();
        return contentType != null && contentType.includes(MediaType.TEXT_HTML);
    }
}

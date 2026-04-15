package com.erzbir.injector.halo.filter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
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
    private final HtmlInjectDispatcher dispatcher;

    public InjectorResponseDecorator(ServerWebExchange exchange,
                                     HtmlInjectDispatcher dispatcher) {
        super(exchange.getResponse());
        this.exchange = exchange;
        this.dispatcher = dispatcher;
    }

    @Override
    @NonNull
    public Mono<Void> writeAndFlushWith(
            @NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {

        ServerHttpResponse response = getDelegate();

        if (!isHtmlResponse(response)) {
            return super.writeAndFlushWith(body);
        }

        String path = exchange.getRequest().getPath().value();
        if (path.isBlank()) {
            return super.writeAndFlushWith(body);
        }

        Flux<DataBuffer> flattenedBody =
                Flux.from(body).flatMapSequential(publisher -> publisher);

        Flux<Publisher<DataBuffer>> processedBody =
                DataBufferUtils.join(flattenedBody)
                        .flatMap(dataBuffer -> processBuffer(dataBuffer, response, path))
                        .flux()
                        .map(Flux::just);

        return super.writeAndFlushWith(processedBody);
    }

    private Mono<DataBuffer> processBuffer(DataBuffer dataBuffer,
                                           ServerHttpResponse response,
                                           String path) {
        try {
            String html = dataBuffer.toString(StandardCharsets.UTF_8);
            if (html.isBlank()) {
                return Mono.just(dataBuffer);
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
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private boolean isHtmlResponse(ServerHttpResponse response) {
        MediaType contentType = response.getHeaders().getContentType();
        return contentType != null && contentType.includes(MediaType.TEXT_HTML);
    }
}
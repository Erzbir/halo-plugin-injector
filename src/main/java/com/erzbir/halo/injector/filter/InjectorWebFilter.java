package com.erzbir.halo.injector.filter;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import com.erzbir.halo.injector.core.ElementIDInjector;
import com.erzbir.halo.injector.core.HTMLInjector;
import com.erzbir.halo.injector.core.InjectService;
import com.erzbir.halo.injector.core.InjectionRule;
import com.erzbir.halo.injector.core.SelectorInjector;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class InjectorWebFilter implements AdditionalWebFilter {
    private final InjectService injectService;
    private final SelectorInjector selectorInjector;
    private final ElementIDInjector elementIDInjector;
    private final ServerWebExchangeMatcher pathMatcher = createPathMatcher();

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return pathMatcher.matches(exchange)
            .flatMap(matchResult -> {
                if (matchResult.isMatch() && shouldInject(exchange)) {
                    String path = exchange.getRequest().getPath().value();
                    return hasMatchingRules(path).flatMap(hasRules -> {
                        if (hasRules) {
                            var decoratedExchange = exchange.mutate()
                                .response(new InjectorResponseDecorator(exchange))
                                .build();
                            return chain.filter(decoratedExchange);
                        }
                        return chain.filter(exchange);
                    });
                }
                return chain.filter(exchange);
            });
    }

    private Mono<Boolean> hasMatchingRules(String path) {
        return Mono.zip(
                injectService.getMatchedRules(path, InjectionRule.Mode.SELECTOR).hasElements(),
                injectService.getMatchedRules(path, InjectionRule.Mode.ID).hasElements()
            ).map(tuple -> tuple.getT1() || tuple.getT2())
            .defaultIfEmpty(false);
    }


    boolean shouldInject(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        var statusCode = response.getStatusCode();
        return statusCode != null && statusCode.isSameCodeAs(HttpStatus.OK);
    }

    ServerWebExchangeMatcher createPathMatcher() {
        var pathMatcher = pathMatchers(HttpMethod.GET, "/**");
        var excludeConsoleMatcher =
            new NegatedServerWebExchangeMatcher(pathMatchers("/console/**", "/uc"));
        var mediaTypeMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        mediaTypeMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));

        return new AndServerWebExchangeMatcher(
            excludeConsoleMatcher,
            pathMatcher,
            mediaTypeMatcher
        );
    }

    public Mono<String> inject(String html, String permalink) {
        return dispatchInject(html, permalink, InjectionRule.Mode.SELECTOR).flatMap(
            ctx -> dispatchInject(ctx, permalink, InjectionRule.Mode.ID)).onErrorResume(e -> {
            log.warn("Failed to inject HTML response", e);
            return Mono.just(html);
        });
    }

    private Mono<String> dispatchInject(
        String html,
        String path,
        InjectionRule.Mode mode) {
        HTMLInjector injector = switch (mode) {
            case SELECTOR -> selectorInjector;
            case ID -> elementIDInjector;
            default -> null;
        };
        if (injector == null) {
            log.warn("No injector found for mode {}", mode);
            return Mono.just(html);
        }

        return injectService.getMatchedRules(path, mode)
            .reduce(html, (ctx, rule) -> {
                String content = injector.inject(html, rule);
                String code = rule.getCode();
                log.info("Injected code: [{}] into [{}]",
                    code.length() > 50 ? code.substring(0, 50) + "..." : code, path);
                return content;
            });
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 100;
    }

    class InjectorResponseDecorator extends ServerHttpResponseDecorator {
        private final ServerWebExchange exchange;

        public InjectorResponseDecorator(ServerWebExchange exchange) {
            super(exchange.getResponse());
            this.exchange = exchange;
        }

        boolean isHtmlResponse(ServerHttpResponse response) {
            return response.getHeaders().getContentType() != null &&
                response.getHeaders().getContentType().includes(MediaType.TEXT_HTML);
        }

        @Override
        @NonNull
        public Mono<Void> writeAndFlushWith(
            @NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
            var response = getDelegate();
            if (!isHtmlResponse(response)) {
                return super.writeAndFlushWith(body);
            }
            String path = exchange.getRequest().getPath().value();
            var flattenedBody = Flux.from(body).flatMapSequential(publisher -> publisher);
            var processedBody = DataBufferUtils.join(flattenedBody).flatMap(dataBuffer -> {
                try {
                    String html = dataBuffer.toString(StandardCharsets.UTF_8);
                    return inject(html, path).onErrorResume(e -> Mono.just(html))
                        .map(processedHtml -> {
                            byte[] resultBytes = processedHtml.getBytes(StandardCharsets.UTF_8);
                            return response.bufferFactory().wrap(resultBytes);
                        });
                } finally {
                    DataBufferUtils.release(dataBuffer);
                }
            }).flux().map(Flux::just);
            return super.writeAndFlushWith(processedBody);
        }
    }
}
package com.erzbir.injector.halo.filter;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.InjectHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class InjectorWebFilter implements AdditionalWebFilter {

    private final InjectHelper injectHelper;
    private final HtmlInjectDispatcher dispatcher;
    private final ServerWebExchangeMatcher pathMatcher;

    public InjectorWebFilter(InjectHelper injectHelper, HtmlInjectDispatcher dispatcher,
                             PathMatcherFactory matcherFactory) {
        this.injectHelper = injectHelper;
        this.dispatcher = dispatcher;
        this.pathMatcher = matcherFactory.create();
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange,
                                      @NonNull WebFilterChain chain) {
        return pathMatcher.matches(exchange)
                .flatMap(matchResult -> {
                    if (!matchResult.isMatch() || !isOkResponse(exchange)) {
                        return chain.filter(exchange);
                    }
                    String path = exchange.getRequest().getPath().value();
                    return hasMatchingRules(path).flatMap(hasRules -> {
                        if (!hasRules) {
                            return chain.filter(exchange);
                        }
                        var decorated = exchange.mutate()
                                .response(new InjectorResponseDecorator(exchange, dispatcher))
                                .build();
                        return chain.filter(decorated);
                    });
                });
    }

    private boolean isOkResponse(ServerWebExchange exchange) {
        var statusCode = exchange.getResponse().getStatusCode();
        return statusCode != null && statusCode.isSameCodeAs(HttpStatus.OK);
    }

    private Mono<Boolean> hasMatchingRules(String path) {
        return Mono.zip(
                        injectHelper.getMatchedRules(path, InjectMode.SELECTOR).hasElements(),
                        injectHelper.getMatchedRules(path, InjectMode.ID).hasElements()
                )
                .map(tuple -> tuple.getT1() || tuple.getT2())
                .defaultIfEmpty(false);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 100;
    }
}
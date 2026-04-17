package com.erzbir.injector.halo.filter;

import com.erzbir.injector.halo.core.InjectHelper;
import org.jspecify.annotations.NonNull;
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

    private final HTMLInjectDispatcher dispatcher;
    private final ServerWebExchangeMatcher pathMatcher;

    public InjectorWebFilter(InjectHelper injectHelper) {
        this.dispatcher = new HTMLInjectDispatcher(injectHelper);
        this.pathMatcher = PathMatcherFactory.create();
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange,
                                      @NonNull WebFilterChain chain) {
        return pathMatcher.matches(exchange)
                .flatMap(matchResult -> {
                    if (!matchResult.isMatch()) {
                        return chain.filter(exchange);
                    }
                    ServerWebExchange decorated = exchange.mutate()
                            .response(new InjectorResponseDecorator(exchange, dispatcher))
                            .build();
                    return chain.filter(decorated);
                });
    }
}

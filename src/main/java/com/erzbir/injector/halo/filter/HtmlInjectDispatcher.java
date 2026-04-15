package com.erzbir.injector.halo.filter;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.ElementIDInjector;
import com.erzbir.injector.halo.core.HTMLCode;
import com.erzbir.injector.halo.core.HTMLInjector;
import com.erzbir.injector.halo.core.SelectorInjector;
import com.erzbir.injector.halo.scheme.InjectionRule;
import com.erzbir.injector.halo.util.InjectHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlInjectDispatcher {

    private final InjectHelper injectHelper;
    private final SelectorInjector selectorInjector;
    private final ElementIDInjector elementIDInjector;

    public Mono<String> dispatch(String html, String permalink) {
        return applyMode(html, permalink, InjectMode.SELECTOR)
                .flatMap(intermediate -> applyMode(intermediate, permalink, InjectMode.ID))
                .onErrorResume(e -> {
                    log.warn("Failed to inject HTML for path [{}]", permalink, e);
                    return Mono.just(html);
                });
    }

    private Mono<String> applyMode(String html, String path, InjectMode mode) {
        HTMLInjector injector = resolveInjector(mode);
        if (injector == null) {
            log.warn("No injector found for mode {}", mode);
            return Mono.just(html);
        }

        return injectHelper.getMatchedRules(path, mode)
                .concatMap(rule ->
                        injectHelper.getConcatCode(rule)
                                .map(code -> new Object[]{rule, code})
                )
                .reduce(html, (ctx, tuple) -> {
                    InjectionRule rule = (InjectionRule) tuple[0];
                    String code = (String) tuple[1];
                    log.debug("Injected rule [{}] into [{}]", rule.getId(), path);
                    return injector.inject(ctx, new HTMLCode(code), rule, null);
                });
    }

    private HTMLInjector resolveInjector(InjectMode mode) {
        return switch (mode) {
            case SELECTOR -> selectorInjector;
            case ID -> elementIDInjector;
            default -> null;
        };
    }
}
package com.erzbir.halo.injector.filter;

import com.erzbir.halo.injector.core.ElementIDInjector;
import com.erzbir.halo.injector.core.HTMLInjector;
import com.erzbir.halo.injector.core.SelectorInjector;
import com.erzbir.halo.injector.scheme.InjectionRule;
import com.erzbir.halo.injector.util.InjectHelper;
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
        return applyMode(html, permalink, InjectionRule.Mode.SELECTOR)
                .flatMap(intermediate -> applyMode(intermediate, permalink, InjectionRule.Mode.ID))
                .onErrorResume(e -> {
                    log.warn("Failed to inject HTML for path [{}]", permalink, e);
                    return Mono.just(html);
                });
    }

    private Mono<String> applyMode(String html, String path, InjectionRule.Mode mode) {
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
                    return injector.inject(ctx, rule.getMatch(), code, rule.getPosition());
                });
    }

    private HTMLInjector resolveInjector(InjectionRule.Mode mode) {
        return switch (mode) {
            case SELECTOR -> selectorInjector;
            case ID -> elementIDInjector;
            default -> null;
        };
    }
}
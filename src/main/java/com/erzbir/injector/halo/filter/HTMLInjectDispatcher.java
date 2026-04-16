package com.erzbir.injector.halo.filter;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.*;
import com.erzbir.injector.halo.scheme.InjectionRule;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HTMLInjectDispatcher {
    private final InjectHelper injectHelper;
    private final Map<InjectMode, HTMLInjector> injectorMap;

    public HTMLInjectDispatcher(InjectHelper injectHelper) {
        this.injectHelper = injectHelper;
        this.injectorMap = new EnumMap<>(InjectMode.class);
        this.injectorMap.put(InjectMode.SELECTOR, new SelectorInjector());
        this.injectorMap.put(InjectMode.ID, new ElementIDInjector());
    }

    public Mono<String> dispatch(String html, String permalink) {
        Document document = Jsoup.parse(html);

        return collectAllRuleCodes(permalink)
                .collectList()
                .flatMap(ruleCodes -> {
                    if (ruleCodes.isEmpty()) {
                        return Mono.just(html);
                    }
                    return Mono.fromCallable(() -> applyRuleCodes(document, permalink, ruleCodes));
                })
                .onErrorResume(e -> {
                    log.warn("Failed to inject HTML for path [{}]", permalink, e);
                    return Mono.just(html);
                });
    }

    private Flux<RuleCode> collectAllRuleCodes(String permalink) {
        return Flux.fromArray(InjectMode.values())
                .flatMap(mode -> fetchRuleCodes(permalink, mode));
    }

    private Flux<RuleCode> fetchRuleCodes(String path, InjectMode mode) {
        var matchedRules = injectHelper.getMatchedRules(path, mode);
        if (matchedRules == null) {
            return Flux.empty();
        }
        return matchedRules
                .concatMap(rule -> injectHelper.getConcatCode(rule)
                        .map(code -> new RuleCode(rule, code, mode)))
                .filter(rc -> !rc.code().isBlank());
    }

    private String applyRuleCodes(Document document, String path, List<RuleCode> ruleCodes) {
        Document.OutputSettings outputSettings = document.outputSettings();
        for (RuleCode rc : ruleCodes) {
            HTMLInjector injector = injectorMap.get(rc.mode());
            if (injector == null) {
                log.warn("No injector found for mode {}", rc.mode());
                continue;
            }
            log.debug("Injecting rule [{}] into [{}]", rc.rule().getId(), path);
            injector.inject(document, new HTMLCode(rc.code()), rc.rule(), null);
            // reset to default
            document.outputSettings(outputSettings);
        }
        return document.html();
    }

    private record RuleCode(InjectionRule rule, String code, InjectMode mode) {
    }
}
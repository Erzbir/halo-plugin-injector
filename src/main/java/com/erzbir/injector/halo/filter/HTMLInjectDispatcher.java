package com.erzbir.injector.halo.filter;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.ElementIDInjector;
import com.erzbir.injector.halo.core.HTMLCode;
import com.erzbir.injector.halo.core.HTMLInjector;
import com.erzbir.injector.halo.core.InjectHelper;
import com.erzbir.injector.halo.core.SelectorInjector;
import com.erzbir.injector.halo.scheme.InjectionRule;
import com.erzbir.injector.halo.util.FingerprintUtil;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
class HTMLInjectDispatcher {
    private static final List<InjectMode> HTML_INJECT_MODES =
        List.of(InjectMode.SELECTOR, InjectMode.ID);
    private final InjectHelper injectHelper;
    private final Map<InjectMode, HTMLInjector> injectorMap;

    public HTMLInjectDispatcher(InjectHelper injectHelper) {
        this.injectHelper = injectHelper;
        this.injectorMap = new EnumMap<>(InjectMode.class);
        this.injectorMap.put(InjectMode.SELECTOR, new SelectorInjector());
        this.injectorMap.put(InjectMode.ID, new ElementIDInjector());
    }

    public Mono<String> dispatch(String html, String permalink) {
        return collectAllRuleCodes(permalink)
            .collectList()
            .flatMap(ruleCodes -> {
                if (ruleCodes.isEmpty()) {
                    return Mono.just(html);
                }
                long htmlFingerprint = FingerprintUtil.fnv1a64(html);
                long ruleFingerprint = buildRuleFingerprint(ruleCodes);
                long cacheFingerprint = Long.rotateLeft(htmlFingerprint, 17) ^ ruleFingerprint;
                String cached = HTMLResponseCache.get(permalink, cacheFingerprint);
                if (cached != null) {
                    log.debug("Return cached injection result for {}", permalink);
                    return Mono.just(cached);
                }
                return Mono.fromCallable(() -> {
                        Document document = Jsoup.parse(html);
                        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        return applyRuleCodes(document, permalink, ruleCodes);
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess(processed ->
                        HTMLResponseCache.put(permalink, cacheFingerprint, processed)
                    );
            });
    }

    private Flux<RuleCode> collectAllRuleCodes(String permalink) {
        return Flux.fromIterable(HTML_INJECT_MODES)
            .concatMap(mode -> fetchRuleCodes(permalink, mode));
    }

    private Flux<RuleCode> fetchRuleCodes(String path, InjectMode mode) {
        var matchedRules = injectHelper.getMatchedRules(path, mode);
        if (matchedRules == null) {
            return Flux.empty();
        }
        return matchedRules.concatMap(rule -> injectHelper.getConcatCode(rule)
                .map(code -> new RuleCode(rule, code)))
            .filter(rc -> !rc.code().isBlank());
    }

    private String applyRuleCodes(Document document, String path, List<RuleCode> ruleCodes) {
        Document.OutputSettings outputSettings = document.outputSettings();
        for (RuleCode rc : ruleCodes) {
            HTMLInjector injector = injectorMap.get(rc.rule().getMode());
            if (injector == null) {
                log.debug("No injector found for rule {}", rc.rule().getId());
                continue;
            }
            log.debug("Injecting rule [{}] into [{}]", rc.rule().getId(), path);
            try {
                injector.inject(document, new HTMLCode(rc.code()), rc.rule(), null);
            } catch (Exception e) {
                log.warn("Injection failed for path [{}] with rule [{}]", path, rc.rule().getId(),
                    e);
            }
            log.debug("Injected rule [{}] into [{}]", rc.rule().getId(), path);
            document.outputSettings(outputSettings);
        }
        return document.html();
    }

    private Long buildRuleFingerprint(List<RuleCode> ruleCodes) {
        if (ruleCodes == null || ruleCodes.isEmpty()) {
            return 0L;
        }
        return ruleCodes.stream()
            .map(RuleCode::fpString)
            .map(FingerprintUtil::fnv1a64)
            .sorted()
            .reduce(0L, (a, b) -> {
                long rotated = (a << 17) | (a >>> 47);
                return rotated ^ b;
            });
    }

    private record RuleCode(InjectionRule rule, String code) {
        String fpString() {
            return rule.getId()
                + "|" + rule.getMode()
                + "|" + rule.getMatch()
                + "|" + rule.getPosition()
                + "|" + rule.getMatchRule()
                + "|" + rule.getSnippetIds().stream().sorted().collect(Collectors.joining(","))
                + "|" + code;
        }
    }
}
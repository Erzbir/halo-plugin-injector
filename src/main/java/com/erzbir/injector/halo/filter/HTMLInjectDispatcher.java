package com.erzbir.injector.halo.filter;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.*;
import com.erzbir.injector.halo.scheme.InjectionRule;
import com.erzbir.injector.halo.util.FingerprintUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
class HTMLInjectDispatcher {
    private final InjectHelper injectHelper;
    private final Map<InjectMode, HTMLInjector> injectorMap;
    private final ConcurrentHashMap<String, Long> ruleFingerprintsMap = new ConcurrentHashMap<>();

    public HTMLInjectDispatcher(InjectHelper injectHelper) {
        this.injectHelper = injectHelper;
        this.injectorMap = new EnumMap<>(InjectMode.class);
        this.injectorMap.put(InjectMode.SELECTOR, new SelectorInjector());
        this.injectorMap.put(InjectMode.ID, new ElementIDInjector());
    }

    public Mono<String> dispatch(String html, String permalink) {
        AtomicLong fingerprintHolder = new AtomicLong(0L);
        return collectAllRuleCodes(permalink)
                .collectList()
                .flatMap(ruleCodes -> {
                    if (ruleCodes.isEmpty()) {
                        return Mono.just(html);
                    }
                    long fingerprint = getOrComputeFingerprint(html, fingerprintHolder);
                    String cached = HTMLResponseCache.get(permalink, fingerprint);
                    if (cached != null) {
                        return Mono.just(cached);
                    }
                    Document document = Jsoup.parse(html);
                    return Mono.fromCallable(() -> applyRuleCodes(document, permalink, ruleCodes))
                            .doOnNext(processed -> HTMLResponseCache.put(
                                    permalink,
                                    fingerprint,
                                    processed
                            ));
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
                        .map(code -> new RuleCode(rule, code)))
                .filter(rc -> !rc.code().isBlank());
    }

    private String applyRuleCodes(Document document, String path, List<RuleCode> ruleCodes) {
        Document.OutputSettings outputSettings = document.outputSettings();

        Long prev = ruleFingerprintsMap.get(path);
        Long cur = buildRuleFingerprints(ruleCodes);
        if (prev == null || prev == 0L) {
            ruleFingerprintsMap.put(path, cur);
        } else {
            if (!prev.equals(cur)) {
                HTMLResponseCache.invalidateCache(path);
                ruleFingerprintsMap.put(path, cur);
            }
        }

        for (RuleCode rc : ruleCodes) {
            HTMLInjector injector = injectorMap.get(rc.rule().getMode());
            if (injector == null) {
                log.warn("No injector found for mode {}", rc.rule().getMode());
                continue;
            }
            log.debug("Injecting rule [{}] into [{}]", rc.rule().getId(), path);
            injector.inject(document, new HTMLCode(rc.code()), rc.rule(), null);
            // reset to default
            document.outputSettings(outputSettings);
        }
        return document.html();
    }

    private Long buildRuleFingerprints(List<RuleCode> ruleCodes) {
        if (ruleCodes == null || ruleCodes.isEmpty()) {
            return 0L;
        }
        return ruleCodes.stream()
                .map(RuleCode::fpString)
                .map(FingerprintUtil::fnv1a64)
                .sorted()
                .reduce(0L, (a, b) -> FingerprintUtil.fnv1a64(a + "," + b));
    }

    private long getOrComputeFingerprint(String html, AtomicLong holder) {
        long existing = holder.get();
        if (existing != 0L) {
            return existing;
        }
        long computed = FingerprintUtil.fnv1a64(html);
        if (holder.compareAndSet(0L, computed)) {
            return computed;
        }
        return holder.get();
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

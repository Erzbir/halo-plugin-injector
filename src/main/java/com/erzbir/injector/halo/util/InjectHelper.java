package com.erzbir.injector.halo.util;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.manager.CodeSnippetManager;
import com.erzbir.injector.halo.manager.InjectionRuleManager;
import com.erzbir.injector.halo.scheme.CodeSnippet;
import com.erzbir.injector.halo.scheme.InjectionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InjectHelper {
    private final InjectionRuleManager ruleManager;
    private final CodeSnippetManager snippetManager;
    private final RouteMatcher routeMatcher = createRouteMatcher();
    private final MatchRuleEvaluator matchRuleEvaluator = new MatchRuleEvaluator(routeMatcher);

    public Flux<InjectionRule> getMatchedRules(String targetPath,
                                               InjectMode mode) {
        if (targetPath.isEmpty()) {
            return Flux.empty();
        }

        return ruleManager.listRuleByMode(mode)
                .filter(rule -> rule.isEnabled() && rule.isValid())
                .filter(rule -> matchRuleEvaluator.matches(rule.getMatchRule(), targetPath))
                .onErrorResume(e -> {
                    log.error("Failed to get matched rules for mode: {}", mode, e);
                    return Flux.empty();
                });
    }

    public Mono<String> getConcatCode(InjectionRule rule) {
        return Flux.fromIterable(rule.getSnippetIds())
                .flatMap(snippetManager::get)
                .filter(CodeSnippet::isValid)
                .filter(CodeSnippet::isEnabled)
                .map(CodeSnippet::getCode)
                .reduce("", String::concat);
    }

    private RouteMatcher createRouteMatcher() {
        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        return new PathPatternRouteMatcher(parser);
    }
}

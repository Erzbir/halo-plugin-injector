package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.manager.CodeSnippetManager;
import com.erzbir.halo.injector.manager.InjectionRuleManager;
import com.erzbir.halo.injector.scheme.CodeSnippet;
import com.erzbir.halo.injector.scheme.InjectionRule;
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
    protected final InjectionRuleManager ruleManager;
    protected final CodeSnippetManager snippetManager;
    protected final RouteMatcher routeMatcher = createRouteMatcher();
    protected final MatchRuleEvaluator matchRuleEvaluator = new MatchRuleEvaluator(routeMatcher);


    public Flux<InjectionRule> getMatchedRules(String targetPath,
                                               InjectionRule.Mode mode) {
        return getMatchedRules(targetPath, null, mode);
    }

    public Flux<InjectionRule> getMatchedRules(String targetPath,
                                               String templateId,
                                               InjectionRule.Mode mode) {
        if (targetPath.isEmpty()) {
            return Flux.empty();
        }

        return ruleManager.listRuleByMode(mode)
                .filter(rule -> rule.isEnabled() && rule.isValid())
                .filter(rule -> matchRuleEvaluator.matches(rule.getMatchRule(), targetPath, templateId))
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

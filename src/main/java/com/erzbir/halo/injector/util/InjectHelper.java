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
import org.springframework.web.util.pattern.PatternParseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

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

    public Flux<InjectionRule> getRulesByMode(InjectionRule.Mode mode) {
        return ruleManager.list()
                .filter(rule -> mode.equals(rule.getMode()));
    }

    public Flux<InjectionRule> getMatchedRules(String targetPath,
                                               InjectionRule.Mode mode) {
        if (targetPath.isEmpty()) {
            return Flux.empty();
        }

        return getRulesByMode(mode)
                .filter(rule -> rule.isEnabled() && rule.isValid())
                .filter(rule -> matchesPath(rule.getPathPatterns(), targetPath, routeMatcher))
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

    private boolean matchesPath(Set<InjectionRule.PathMatchRule> pathPatterns,
                                String currentPath,
                                RouteMatcher routeMatcher) {
        if (currentPath == null || pathPatterns == null || pathPatterns.isEmpty()) {
            return false;
        }

        RouteMatcher.Route requestRoute = routeMatcher.parseRoute(currentPath);

        return pathPatterns.stream()
                .filter(pattern -> pattern != null && !pattern.getPathPattern().trim().isEmpty())
                .anyMatch(pattern -> {
                    try {
                        return routeMatcher.match(pattern.getPathPattern(), requestRoute);
                    } catch (PatternParseException e) {
                        log.warn("Parse route pattern [{}] failed for path [{}]", pattern, currentPath,
                                e);
                        return false;
                    }
                });
    }

    private RouteMatcher createRouteMatcher() {
        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        return new PathPatternRouteMatcher(parser);
    }
}

package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.BasicConfig;
import com.erzbir.halo.injector.setting.InjectionRule;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.web.IWebRequest;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InjectionService {
    private final ReactiveSettingFetcher reactiveSettingFetcher;
    private final RouteMatcher routeMatcher = createRouteMatcher();

    public Mono<String> getMatchedCodeForLocation(ITemplateContext context,
        InjectionRule.Location targetLocation) {
        return reactiveSettingFetcher.fetch("basic", BasicConfig.class)
            .flatMap(basicConfig -> {
                String currentPath = getCurrentPath(context);
                if (currentPath.isEmpty()) {
                    return Mono.empty();
                }

                List<InjectionRule> locationRules =
                    basicConfig.getRulesByLocation(targetLocation);

                List<InjectionRule> matchedRules = locationRules.stream()
                    .filter(rule -> matchesPath(rule.getPathPatterns(), currentPath, routeMatcher))
                    .toList();

                if (matchedRules.isEmpty()) {
                    return Mono.empty();
                }

                String combinedCode = processAndCombineRules(matchedRules);
                return Mono.just(combinedCode);
            })
            .onErrorResume(e -> {
                log.error("Failed to get matched code for location: {}", targetLocation, e);
                return Mono.empty();
            });
    }

    public boolean matchesPath(List<InjectionRule.PathMatchRule> pathPatterns, String currentPath,
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

    private String processAndCombineRules(List<InjectionRule> rules) {
        StringBuilder combinedCode = new StringBuilder();

        for (InjectionRule rule : rules) {
            String processedCode = processRuleCode(rule);
            if (!processedCode.trim().isEmpty()) {
                if (!combinedCode.isEmpty()) {
                    combinedCode.append("\n");
                }
                combinedCode.append(processedCode);
            }
        }

        return combinedCode.toString();
    }

    private String processRuleCode(InjectionRule rule) {
        String code = rule.getCode();
        String comment_start = "<!-- PluginInjector start -->";
        String comment_end = "<!-- PluginInjector end -->";
        return comment_start + code + comment_end;
    }

    private String getCurrentPath(ITemplateContext context) {
        try {
            if (!Contexts.isWebContext(context)) {
                return "";
            }
            IWebRequest request = Contexts.asWebContext(context).getExchange().getRequest();
            return request.getRequestPath();
        } catch (Exception e) {
            log.debug("Failed to get current path from context", e);
            return "";
        }
    }

    private RouteMatcher createRouteMatcher() {
        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        return new PathPatternRouteMatcher(parser);
    }
}
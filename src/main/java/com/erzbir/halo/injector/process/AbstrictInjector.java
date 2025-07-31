package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.BasicConfig;
import com.erzbir.halo.injector.setting.InjectionRule;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.web.IWebRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstrictInjector implements Injector {
    private final ReactiveSettingFetcher reactiveSettingFetcher;
    private final RouteMatcher routeMatcher = createRouteMatcher();

    public Mono<Void> inject(ITemplateContext context, IModel model,
        InjectionRule.Location location) {
        return getMatchedRulesForLocation(context, location)
            .map(this::processRuleCode)
            .filter(code -> !code.trim().isEmpty())
            .doOnNext(code -> {
                model.add(context.getModelFactory().createText(finalProcessCode(code)));
                log.debug("Injected code: {}",
                    code.length() > 50 ? code.substring(0, 50) + "..." : code);
            })
            .then();
    }

    private Flux<InjectionRule> getMatchedRulesForLocation(ITemplateContext context,
        InjectionRule.Location targetLocation) {
        return reactiveSettingFetcher.fetch("basic", BasicConfig.class)
            .flatMapMany(basicConfig -> {
                String currentPath = getCurrentPath(context);
                if (currentPath.isEmpty()) {
                    return Flux.empty();
                }

                List<InjectionRule> locationRules =
                    basicConfig.getRulesByLocation(targetLocation);

                return Flux.fromIterable(locationRules)
                    .filter(rule -> matchesPath(rule.getPathPatterns(), currentPath, routeMatcher));
            })
            .onErrorResume(e -> {
                log.error("Failed to get matched rules for location: {}", targetLocation, e);
                return Flux.empty();
            });
    }

    private boolean matchesPath(List<InjectionRule.PathMatchRule> pathPatterns,
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

    private String finalProcessCode(String code) {
        String comment_start = "<!-- PluginInjector start -->";
        String comment_end = "<!-- PluginInjector end -->";
        return comment_start + code + comment_end;
    }

    protected String processRuleCode(InjectionRule rule) {
        return rule.getCode();
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




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
import reactor.core.publisher.Flux;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InjectService {
    protected final ReactiveSettingFetcher reactiveSettingFetcher;
    protected final RouteMatcher routeMatcher = createRouteMatcher();

    public Flux<InjectionRule> getMatchedRules(String targetPath,
        InjectionRule.Location targetLocation) {
        return reactiveSettingFetcher.fetch("basic", BasicConfig.class)
            .flatMapMany(basicConfig -> {
                if (targetPath.isEmpty()) {
                    return Flux.empty();
                }
                return Flux.fromStream(basicConfig.getRulesByLocation(targetLocation).stream()
                        .filter(InjectionRule::getEnabled))
                    .filter(rule -> matchesPath(rule.getPathPatterns(), targetPath, routeMatcher));
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

    private RouteMatcher createRouteMatcher() {
        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        return new PathPatternRouteMatcher(parser);
    }
}

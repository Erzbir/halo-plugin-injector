package com.erzbir.halo.injector.core;

import com.erzbir.halo.injector.setting.BasicSettingSupplier;
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

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InjectService {
    protected final BasicSettingSupplier basicSettingSupplier;
    protected final RouteMatcher routeMatcher = createRouteMatcher();

    public Flux<InjectionRule> getRulesByMode(InjectionRule.Mode mode) {
        return basicSettingSupplier.get()
            .flatMapMany(basicConfig -> Flux.fromStream(
                basicConfig.nullSafeRules().stream()
                    .filter(rule -> mode.equals(rule.getMode()))
            ));
    }

    public Flux<InjectionRule> getMatchedRules(String targetPath,
        InjectionRule.Mode mode) {
        return basicSettingSupplier.get()
            .flatMapMany(basicConfig -> {
                if (targetPath.isEmpty()) {
                    return Flux.empty();
                }
                return getRulesByMode(mode)
                    .filter(rule -> rule.getEnabled() && rule.isValid())
                    .filter(rule -> matchesPath(rule.getPathPatterns(), targetPath, routeMatcher));
            })
            .onErrorResume(e -> {
                log.error("Failed to get matched rules for mode: {}", mode, e);
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

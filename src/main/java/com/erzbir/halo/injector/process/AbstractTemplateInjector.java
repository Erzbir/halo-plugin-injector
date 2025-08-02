package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTemplateInjector implements Injector {
    protected final InjectService injectService;
    protected final String START_MARK = "<!-- PluginInjector start -->";
    protected final String END_MAR = "<!-- PluginInjector end -->";

    protected Mono<Void> inject(InjectProvider injectProvider, String targetPath,
                                InjectionRule.Location location) {
        return injectService.getMatchedRules(targetPath, location)
                .map(this::processRuleCode)
                .filter(code -> !code.trim().isEmpty())
                .doOnNext(code -> {
                    injectProvider.inject(START_MARK + code + END_MAR);
                    log.debug("Injected code: {}",
                            code.length() > 50 ? code.substring(0, 50) + "..." : code);
                })
                .then();
    }
}




package com.erzbir.halo.injector.manager;

import com.erzbir.halo.injector.scheme.InjectionRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import run.halo.app.extension.ReactiveExtensionClient;

@Slf4j
@Component
public class InjectionRuleManager {
    private final ReactiveExtensionClient client;

    public InjectionRuleManager(ReactiveExtensionClient client) {
        this.client = client;
    }

    public Flux<InjectionRule> list() {
        return client.list(InjectionRule.class, null, null)
                .doOnError(e -> log.error("Failed to refresh InjectionRule cache", e));
    }
}

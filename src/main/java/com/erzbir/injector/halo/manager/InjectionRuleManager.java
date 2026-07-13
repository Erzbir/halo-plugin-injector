package com.erzbir.injector.halo.manager;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.scheme.InjectionRule;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class InjectionRuleManager {
    private final ReactiveExtensionClient client;

    public InjectionRuleManager(ReactiveExtensionClient client) {
        this.client = client;
    }

    public Flux<InjectionRule> list() {
        return client.list(InjectionRule.class, null, null);
    }

    public Flux<InjectionRule> listRuleByMode(InjectMode mode) {
        return list()
                .filter(rule -> mode.equals(rule.getMode()));
    }
}

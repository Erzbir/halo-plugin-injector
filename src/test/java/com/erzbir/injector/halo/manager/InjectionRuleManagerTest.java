package com.erzbir.injector.halo.manager;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.scheme.InjectionRule;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import run.halo.app.extension.ReactiveExtensionClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InjectionRuleManagerTest {

    @Test
    void shouldDelegateListToReactiveClient() {
        ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
        InjectionRuleManager manager = new InjectionRuleManager(client);
        InjectionRule rule = new InjectionRule();
        when(client.list(InjectionRule.class, null, null)).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = manager.list().collectList().block();

        assertEquals(1, rules.size());
        assertSame(rule, rules.getFirst());
    }

    @Test
    void shouldFilterRulesByMode() {
        ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
        InjectionRuleManager manager = new InjectionRuleManager(client);
        InjectionRule headRule = new InjectionRule();
        headRule.setMode(InjectMode.HEAD);
        InjectionRule footerRule = new InjectionRule();
        footerRule.setMode(InjectMode.FOOTER);
        when(client.list(InjectionRule.class, null, null)).thenReturn(Flux.just(headRule, footerRule));

        List<InjectionRule> rules = manager.listRuleByMode(InjectMode.FOOTER).collectList().block();

        assertEquals(1, rules.size());
        assertSame(footerRule, rules.getFirst());
    }

    @Test
    void shouldPropagateErrorFromClientList() {
        ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
        InjectionRuleManager manager = new InjectionRuleManager(client);
        when(client.list(InjectionRule.class, null, null)).thenReturn(Flux.error(new IllegalStateException("boom")));

        assertThrows(RuntimeException.class, () -> manager.list().collectList().block());
    }
}

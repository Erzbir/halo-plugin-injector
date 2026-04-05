package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.manager.CodeSnippetManager;
import com.erzbir.halo.injector.manager.InjectionRuleManager;
import com.erzbir.halo.injector.scheme.InjectionRule;
import com.erzbir.halo.injector.scheme.MatchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InjectHelperTest {
    @Mock
    private InjectionRuleManager ruleManager;

    @Mock
    private CodeSnippetManager snippetManager;

    private InjectHelper injectHelper;

    @BeforeEach
    void setUp() {
        injectHelper = new InjectHelper(ruleManager, snippetManager);
    }

    @Test
    void shouldMatchRuleWhenPathAndTemplateMatch() {
        InjectionRule rule = createRule(group(MatchRule.Operator.AND,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.templateRule(MatchRule.Matcher.EXACT, "post")));
        when(ruleManager.list()).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = injectHelper
                .getMatchedRules("/posts/demo", "post", InjectionRule.Mode.SELECTOR)
                .collectList()
                .block();

        assertEquals(List.of(rule), rules);
    }

    @Test
    void shouldSkipRuleWhenTemplateDoesNotMatch() {
        InjectionRule rule = createRule(group(MatchRule.Operator.AND,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.templateRule(MatchRule.Matcher.EXACT, "post")));
        when(ruleManager.list()).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = injectHelper
                .getMatchedRules("/posts/demo", "page", InjectionRule.Mode.SELECTOR)
                .collectList()
                .block();

        assertTrue(rules.isEmpty());
    }

    @Test
    void shouldKeepRuleDuringPathPrecheckWhenTemplateIsUnknown() {
        InjectionRule rule = createRule(group(MatchRule.Operator.AND,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.templateRule(MatchRule.Matcher.EXACT, "post")));
        when(ruleManager.list()).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = injectHelper
                .getPathMatchedRules("/posts/demo", InjectionRule.Mode.SELECTOR)
                .collectList()
                .block();

        assertEquals(List.of(rule), rules);
    }

    @Test
    void shouldSkipRuleDuringPathPrecheckWhenPathDefinitelyMisses() {
        InjectionRule rule = createRule(group(MatchRule.Operator.AND,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.templateRule(MatchRule.Matcher.EXACT, "post")));
        when(ruleManager.list()).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = injectHelper
                .getPathMatchedRules("/archives/demo", InjectionRule.Mode.SELECTOR)
                .collectList()
                .block();

        assertTrue(rules.isEmpty());
    }

    @Test
    void shouldKeepRuleDuringPrecheckWhenOrContainsUnknownTemplateBranch() {
        InjectionRule rule = createRule(group(MatchRule.Operator.OR,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.templateRule(MatchRule.Matcher.EXACT, "post")));
        when(ruleManager.list()).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = injectHelper
                .getPathMatchedRules("/archives/demo", InjectionRule.Mode.SELECTOR)
                .collectList()
                .block();

        assertEquals(List.of(rule), rules);
    }

    private InjectionRule createRule(MatchRule matchRule) {
        InjectionRule rule = new InjectionRule();
        rule.setEnabled(true);
        rule.setMode(InjectionRule.Mode.SELECTOR);
        rule.setMatch("main");
        rule.setMatchRule(matchRule);
        return rule;
    }

    private MatchRule group(MatchRule.Operator operator, MatchRule... children) {
        MatchRule rule = new MatchRule();
        rule.setType(MatchRule.Type.GROUP);
        rule.setOperator(operator);
        rule.setChildren(List.of(children));
        return rule;
    }
}

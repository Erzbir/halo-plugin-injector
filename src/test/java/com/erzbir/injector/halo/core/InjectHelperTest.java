package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.api.MatchRule;
import com.erzbir.injector.halo.manager.CodeSnippetManager;
import com.erzbir.injector.halo.manager.InjectionRuleManager;
import com.erzbir.injector.halo.scheme.CodeSnippet;
import com.erzbir.injector.halo.scheme.InjectionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InjectHelperTest {

    @Mock
    private InjectionRuleManager ruleManager;

    @Mock
    private CodeSnippetManager snippetManager;

    private InjectHelper injectHelper;

    private static InjectionRule createRule(boolean enabled, InjectMode mode, MatchRule matchRule) {
        InjectionRule rule = new InjectionRule();
        rule.setEnabled(enabled);
        rule.setMode(mode);
        rule.setMatchRule(matchRule);
        return rule;
    }

    private static CodeSnippet createSnippet(boolean enabled, String code) {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setEnabled(enabled);
        snippet.setCode(code);
        return snippet;
    }

    @BeforeEach
    void setUp() {
        injectHelper = new InjectHelper(ruleManager, snippetManager);
    }

    @Test
    void shouldReturnEmptyWhenTargetPathIsEmpty() {
        List<InjectionRule> rules = injectHelper.getMatchedRules("", InjectMode.HEAD).collectList().block();
        assertNotNull(rules);
        assertTrue(rules.isEmpty());

        verifyNoInteractions(ruleManager);
    }

    @Test
    void shouldFilterOutDisabledInvalidAndUnmatchedRules() {
        InjectionRule matched = createRule(true, InjectMode.HEAD, MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"));
        InjectionRule disabled = createRule(false, InjectMode.HEAD, MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"));
        InjectionRule invalid = createRule(true, InjectMode.HEAD, MatchRule.groupRule(MatchRule.Operator.AND));
        InjectionRule unmatched = createRule(true, InjectMode.HEAD, MatchRule.pathRule(MatchRule.Matcher.ANT, "/archives/**"));

        when(ruleManager.listRuleByMode(InjectMode.HEAD))
                .thenReturn(Flux.just(matched, disabled, invalid, unmatched));

        List<InjectionRule> rules = injectHelper.getMatchedRules("/posts/2026/halo", InjectMode.HEAD)
                .collectList()
                .block();
        assertNotNull(rules);
        assertEquals(1, rules.size());
        assertSame(matched, rules.getFirst());
    }

    @Test
    void shouldReturnEmptyWhenListingRulesFails() {
        when(ruleManager.listRuleByMode(InjectMode.FOOTER))
                .thenReturn(Flux.error(new IllegalStateException("boom")));

        List<InjectionRule> rules = injectHelper.getMatchedRules("/posts/1", InjectMode.FOOTER).collectList().block();
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    void shouldConcatOnlyEnabledAndValidSnippetCode() {
        InjectionRule rule = new InjectionRule();
        rule.setSnippetIds(new LinkedHashSet<>(Arrays.asList("a", "b", "c", "d")));

        CodeSnippet a = createSnippet(true, "<script>a</script>");
        CodeSnippet b = createSnippet(true, " ");
        CodeSnippet c = createSnippet(false, "<script>c</script>");
        CodeSnippet d = createSnippet(true, "<script>d</script>");

        when(snippetManager.get("a")).thenReturn(Mono.just(a));
        when(snippetManager.get("b")).thenReturn(Mono.just(b));
        when(snippetManager.get("c")).thenReturn(Mono.just(c));
        when(snippetManager.get("d")).thenReturn(Mono.just(d));

        String code = injectHelper.getConcatCode(rule).block();
        assertEquals("<script>a</script><script>d</script>", code);
    }

    @Test
    void shouldReturnEmptyStringWhenRuleHasNoSnippetIds() {
        InjectionRule rule = new InjectionRule();

        String code = injectHelper.getConcatCode(rule).block();
        assertEquals("", code);
    }

    @Test
    void shouldPropagateErrorWhenSnippetLoadingFails() {
        InjectionRule rule = new InjectionRule();
        rule.setSnippetIds(new LinkedHashSet<>(List.of("s1")));
        when(snippetManager.get("s1")).thenReturn(Mono.error(new IllegalStateException("load failed")));

        assertThrows(RuntimeException.class, () -> injectHelper.getConcatCode(rule).block());
    }

    @Test
    void shouldMatchRuleUsingDefaultMatchRuleWhenRuleMatchRuleIsNull() {
        InjectionRule rule = createRule(true, InjectMode.HEAD, null);
        when(ruleManager.listRuleByMode(InjectMode.HEAD)).thenReturn(Flux.just(rule));

        List<InjectionRule> rules = injectHelper.getMatchedRules("/any/path", InjectMode.HEAD)
                .collectList()
                .block();

        assertNotNull(rules);
        assertEquals(1, rules.size());
        assertSame(rule, rules.getFirst());
    }
}

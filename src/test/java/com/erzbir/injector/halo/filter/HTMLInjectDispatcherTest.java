package com.erzbir.injector.halo.filter;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.api.InjectPosition;
import com.erzbir.injector.halo.core.InjectHelper;
import com.erzbir.injector.halo.scheme.InjectionRule;
import com.erzbir.injector.halo.scheme.MatchRule;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class HTMLInjectDispatcherTest {

    @Test
    void shouldApplySelectorThenIdRules() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        HTMLInjectDispatcher dispatcher = new HTMLInjectDispatcher(injectHelper);

        InjectionRule selectorRule = mock(InjectionRule.class);
        InjectionRule idRule = mock(InjectionRule.class);
        when(selectorRule.getId()).thenReturn("selector-rule");
        when(idRule.getId()).thenReturn("id-rule");
        when(selectorRule.getMode()).thenReturn(InjectMode.SELECTOR);
        when(idRule.getMode()).thenReturn(InjectMode.ID);
        when(selectorRule.getMatch()).thenReturn(".entry");
        when(idRule.getMatch()).thenReturn("target");
        when(selectorRule.getPosition()).thenReturn(InjectPosition.APPEND);
        when(idRule.getPosition()).thenReturn(InjectPosition.APPEND);
        when(selectorRule.getMatchRule()).thenReturn(MatchRule.defaultRule());
        when(idRule.getMatchRule()).thenReturn(MatchRule.defaultRule());
        when(selectorRule.getSnippetIds()).thenReturn(Set.of("s1"));
        when(idRule.getSnippetIds()).thenReturn(Set.of("s2"));

        when(injectHelper.getMatchedRules("/p/1", InjectMode.SELECTOR)).thenReturn(Flux.just(selectorRule));
        when(injectHelper.getMatchedRules("/p/1", InjectMode.ID)).thenReturn(Flux.just(idRule));
        when(injectHelper.getConcatCode(selectorRule)).thenReturn(Mono.just("<span>S</span>"));
        when(injectHelper.getConcatCode(idRule)).thenReturn(Mono.just("<span>I</span>"));

        String html = "<html><body><div class='entry'></div><div id='target'></div></body></html>";
        String result = dispatcher.dispatch(html, "/p/1").block();
        var doc = Jsoup.parse(result);
        var entry = doc.selectFirst(".entry");
        var target = doc.getElementById("target");

        assertNotNull(entry);
        assertNotNull(target);
        assertTrue(entry.html().contains("<span>S</span>"));
        assertTrue(target.html().contains("<span>I</span>"));
    }

    @Test
    void shouldRecomputeWhenRuleCodeChanges() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        HTMLInjectDispatcher dispatcher = new HTMLInjectDispatcher(injectHelper);

        InjectionRule selectorRule = mock(InjectionRule.class);
        when(selectorRule.getId()).thenReturn("changing-rule");
        when(selectorRule.getMode()).thenReturn(InjectMode.SELECTOR);
        when(selectorRule.getMatch()).thenReturn(".entry");
        when(selectorRule.getPosition()).thenReturn(InjectPosition.APPEND);
        when(selectorRule.getMatchRule()).thenReturn(MatchRule.defaultRule());
        when(selectorRule.getSnippetIds()).thenReturn(Set.of("s1"));
        when(injectHelper.getMatchedRules("/cache/rule-change", InjectMode.SELECTOR))
            .thenReturn(Flux.just(selectorRule));
        when(injectHelper.getMatchedRules("/cache/rule-change", InjectMode.ID))
            .thenReturn(Flux.empty());
        when(injectHelper.getConcatCode(selectorRule))
            .thenReturn(Mono.just("<span>before</span>"), Mono.just("<span>after</span>"));

        String html = "<html><body><div class='entry'></div></body></html>";
        String first = dispatcher.dispatch(html, "/cache/rule-change").block();
        String second = dispatcher.dispatch(html, "/cache/rule-change").block();

        assertTrue(first.contains("<span>before</span>"));
        assertTrue(second.contains("<span>after</span>"));
        assertFalse(second.contains("<span>before</span>"));
    }

    @Test
    void shouldRecomputeWhenRuleOrderChanges() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        HTMLInjectDispatcher dispatcher = new HTMLInjectDispatcher(injectHelper);
        InjectionRule firstRule = selectorRule("first-rule", "s1");
        InjectionRule secondRule = selectorRule("second-rule", "s2");
        when(injectHelper.getMatchedRules("/cache/rule-order", InjectMode.SELECTOR))
            .thenReturn(Flux.just(firstRule, secondRule), Flux.just(secondRule, firstRule));
        when(injectHelper.getMatchedRules("/cache/rule-order", InjectMode.ID))
            .thenReturn(Flux.empty());
        when(injectHelper.getConcatCode(firstRule)).thenReturn(Mono.just("<span>A</span>"));
        when(injectHelper.getConcatCode(secondRule)).thenReturn(Mono.just("<span>B</span>"));

        String html = "<html><body><div class='entry'></div></body></html>";
        var first = Jsoup.parse(dispatcher.dispatch(html, "/cache/rule-order").block());
        var second = Jsoup.parse(dispatcher.dispatch(html, "/cache/rule-order").block());

        String firstHtml = first.selectFirst(".entry").html();
        String secondHtml = second.selectFirst(".entry").html();
        assertTrue(firstHtml.indexOf("<span>A</span>") < firstHtml.indexOf("<span>B</span>"));
        assertTrue(secondHtml.indexOf("<span>B</span>") < secondHtml.indexOf("<span>A</span>"));
    }

    @Test
    void shouldReturnOriginalHtmlWhenNoRulesMatch() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        HTMLInjectDispatcher dispatcher = new HTMLInjectDispatcher(injectHelper);
        when(injectHelper.getMatchedRules("/p/1", InjectMode.SELECTOR)).thenReturn(Flux.empty());
        when(injectHelper.getMatchedRules("/p/1", InjectMode.ID)).thenReturn(Flux.empty());

        String result = dispatcher.dispatch("<html><head></head><body>raw</body></html>", "/p/1").block();

        assertEquals("<html><head></head><body>raw</body></html>", result);
        verify(injectHelper, never()).getMatchedRules("/p/1", InjectMode.HEAD);
        verify(injectHelper, never()).getMatchedRules("/p/1", InjectMode.FOOTER);
    }

    @Test
    void shouldReturnOriginalHtmlWhenInjectorThrows() {
        InjectHelper injectHelper = mock(InjectHelper.class);
        HTMLInjectDispatcher dispatcher = new HTMLInjectDispatcher(injectHelper);

        InjectionRule selectorRule = mock(InjectionRule.class);
        when(selectorRule.getId()).thenReturn("selector-rule");
        when(selectorRule.getMode()).thenReturn(InjectMode.SELECTOR);
        when(selectorRule.getMatch()).thenReturn("[");
        when(selectorRule.getPosition()).thenReturn(InjectPosition.APPEND);
        when(selectorRule.getMatchRule()).thenReturn(MatchRule.defaultRule());
        when(selectorRule.getSnippetIds()).thenReturn(Set.of("s1"));
        when(injectHelper.getMatchedRules("/p/1", InjectMode.SELECTOR)).thenReturn(Flux.just(selectorRule));
        when(injectHelper.getMatchedRules("/p/1", InjectMode.ID)).thenReturn(Flux.empty());
        when(injectHelper.getConcatCode(selectorRule)).thenReturn(Mono.just("S"));

        String result = dispatcher.dispatch("<html><head></head><body>raw</body></html>", "/p/1").block();

        assertEquals("<html><head></head><body>raw</body></html>", result);
    }

    private InjectionRule selectorRule(String id, String snippetId) {
        InjectionRule rule = mock(InjectionRule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getMode()).thenReturn(InjectMode.SELECTOR);
        when(rule.getMatch()).thenReturn(".entry");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        when(rule.getMatchRule()).thenReturn(MatchRule.defaultRule());
        when(rule.getSnippetIds()).thenReturn(Set.of(snippetId));
        return rule;
    }
}

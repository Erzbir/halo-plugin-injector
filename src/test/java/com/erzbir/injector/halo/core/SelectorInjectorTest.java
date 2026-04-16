package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.api.InjectPosition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Selector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectorInjectorTest {

    private static final String START = "<!-- PluginInjector start -->";
    private static final String END = "<!-- PluginInjector end -->";
    private static final String SCRIPT = "<script>ok</script>";

    private final SelectorInjector injector = new SelectorInjector();

    @ParameterizedTest
    @EnumSource(InjectPosition.class)
    void shouldInjectCodeAtExpectedPosition_whenSelectorMatches(InjectPosition position) {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn(".target");
        when(rule.getPosition()).thenReturn(position);
        Document html = Jsoup.parse("<html><body><div class='target'>origin</div></body></html>");

        String result = injector.inject(html, new HTMLCode(SCRIPT), rule, null);

        assertPosition(result, position, "div class=\"target\">origin</div>");
    }

    @Test
    void shouldKeepHtmlUnchangedWhenSelectorNotFound() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn(".missing");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        Document html = Jsoup.parse("<html><body><div class='target'>origin</div></body></html>");

        String result = injector.inject(html, new HTMLCode(SCRIPT), rule, null);

        assertTrue(result.contains("<div class=\"target\">origin</div>"));
        assertFalse(result.contains(START));
        assertFalse(result.contains(END));
    }

    @Test
    void shouldThrowExceptionWhenSelectorIsInvalid() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn("[");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        Document html = Jsoup.parse("<html><body><div class='target'>origin</div></body></html>");

        assertThrows(Selector.SelectorParseException.class,
                () -> injector.inject(html, new HTMLCode(SCRIPT), rule, null));
    }

    @Test
    void shouldInjectIntoAllMatchedElements_whenMultipleTargetsFound() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn(".target");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        Document html = Jsoup.parse("<html><body><div class='target'>a</div><div class='target'>b</div></body></html>");

        String result = injector.inject(html, new HTMLCode(SCRIPT), rule, null);

        assertEquals(2, countOccurrences(result, START));
        assertEquals(2, countOccurrences(result, END));
    }

    private void assertPosition(String html, InjectPosition position, String targetTagSuffix) {
        String wrapped = START + SCRIPT + END;
        String target = "<" + targetTagSuffix;
        switch (position) {
            case APPEND -> assertTrue(html.contains("origin" + wrapped + "</div>"));
            case PREPEND -> assertTrue(html.contains(">" + wrapped + "origin</div>"));
            case BEFORE -> assertTrue(html.contains(wrapped + target));
            case AFTER -> assertTrue(html.contains(target + wrapped));
            case REPLACE -> {
                assertTrue(html.contains(wrapped));
                assertFalse(html.contains(target));
            }
        }
    }

    private int countOccurrences(String input, String token) {
        return input.split(java.util.regex.Pattern.quote(token), -1).length - 1;
    }
}

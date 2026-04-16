package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.api.InjectPosition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElementIDInjectorTest {

    private static final String START = "<!-- PluginInjector start -->";
    private static final String END = "<!-- PluginInjector end -->";
    private static final String SCRIPT = "<script>ok</script>";

    private final ElementIDInjector injector = new ElementIDInjector();

    @ParameterizedTest
    @EnumSource(InjectPosition.class)
    void shouldInjectCodeAtExpectedPosition_whenIdMatches(InjectPosition position) {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn("target");
        when(rule.getPosition()).thenReturn(position);
        Document html = Jsoup.parse("<html><body><div id='target'>origin</div></body></html>");

        String result = injector.inject(html, new HTMLCode(SCRIPT), rule, null);

        assertPosition(result, position, "div id=\"target\">origin</div>");
    }

    @Test
    void shouldKeepHtmlUnchangedWhenIdNotFound() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn("missing");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        Document html = Jsoup.parse("<html><body><div id='target'>origin</div></body></html>");

        String result = injector.inject(html, new HTMLCode(SCRIPT), rule, null);

        assertTrue(result.contains("<div id=\"target\">origin</div>"));
        assertFalse(result.contains(START));
        assertFalse(result.contains(END));
    }

    @Test
    void shouldThrowExceptionWhenPositionIsNull() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn("target");
        when(rule.getPosition()).thenReturn(null);
        Document html = Jsoup.parse("<html><body><div id='target'>origin</div></body></html>");

        assertThrows(NullPointerException.class,
                () -> injector.inject(html, new HTMLCode(SCRIPT), rule, null));
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
}

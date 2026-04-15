package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.InjectPosition;
import com.erzbir.injector.api.IInjectionRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElementIDInjectorTest {

    private final ElementIDInjector injector = new ElementIDInjector();

    @Test
    void shouldInjectCodeIntoMatchedId() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn("target");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);

        String result = injector.inject(
                "<html><body><div id='target'>origin</div></body></html>",
                new HTMLCode("<script>ok</script>"),
                rule,
                null
        );

        assertTrue(result.contains("origin<!-- PluginInjector start --><script>ok</script><!-- PluginInjector end -->"));
    }

    @Test
    void shouldKeepHtmlUnchangedWhenIdNotFound() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn("not-found");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        String html = "<html><body><div id='target'>origin</div></body></html>";

        String result = injector.inject(html, new HTMLCode("<script>ok</script>"), rule, null);

        assertTrue(result.contains("origin"));
        assertFalse(result.contains("PluginInjector start"));
    }
}

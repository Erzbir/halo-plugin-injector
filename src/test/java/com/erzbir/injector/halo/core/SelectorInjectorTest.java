package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.InjectPosition;
import com.erzbir.injector.api.IInjectionRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectorInjectorTest {

    private final SelectorInjector injector = new SelectorInjector();

    @Test
    void shouldInjectCodeIntoMatchedSelector() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn(".target");
        when(rule.getPosition()).thenReturn(InjectPosition.PREPEND);

        String result = injector.inject(
                "<html><body><div class='target'>origin</div></body></html>",
                new HTMLCode("<script>ok</script>"),
                rule,
                null
        );

        assertTrue(result.contains("<div class=\"target\"><!-- PluginInjector start --><script>ok</script><!-- PluginInjector end -->origin</div>"));
    }

    @Test
    void shouldKeepHtmlUnchangedWhenSelectorNotFound() {
        IInjectionRule rule = mock(IInjectionRule.class);
        when(rule.getMatch()).thenReturn(".missing");
        when(rule.getPosition()).thenReturn(InjectPosition.APPEND);
        String html = "<html><body><div class='target'>origin</div></body></html>";

        String result = injector.inject(html, new HTMLCode("<script>ok</script>"), rule, null);

        assertTrue(result.contains("origin"));
        assertFalse(result.contains("PluginInjector start"));
    }
}

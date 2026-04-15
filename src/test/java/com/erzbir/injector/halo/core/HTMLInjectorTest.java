package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class HTMLInjectorTest {

    @Test
    void shouldInheritProcessCodeDefaultMethod_whenUsingHtmlInjectorImplementation() {
        HTMLInjector injector = (target, code, rule, context) -> target;

        String result = injector.processCode("<script>x</script>");

        assertEquals("<!-- PluginInjector start --><script>x</script><!-- PluginInjector end -->", result);
    }

    @Test
    void shouldReturnSameTarget_whenImplementationIsIdentity() {
        HTMLInjector injector = (target, code, rule, context) -> target;
        String target = "<html></html>";
        Code code = () -> "<script>x</script>";
        IInjectionRule rule = null;

        String result = injector.inject(target, code, rule, null);

        assertSame(target, result);
    }
}

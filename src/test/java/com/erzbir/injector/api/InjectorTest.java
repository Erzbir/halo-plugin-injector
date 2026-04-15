package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InjectorTest {

    @Test
    void shouldWrapCodeWithPluginMarkers() {
        Injector<Void, Void> injector = (target, code, rule, context) -> null;

        String result = injector.processCode("<script>x</script>");

        assertEquals("<!-- PluginInjector start --><script>x</script><!-- PluginInjector end -->", result);
    }
}

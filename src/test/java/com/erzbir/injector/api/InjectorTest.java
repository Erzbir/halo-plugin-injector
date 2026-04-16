package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InjectorTest {

    @Test
    void shouldKeepCodeUnchangedByDefault() {
        Injector<Void, Void, Void> injector = (target, code, rule, context) -> null;

        String result = injector.processCode("<script>x</script>");

        assertEquals("<script>x</script>", result);
    }
}

package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CodeTest {

    @Test
    void shouldExposeRawValue_whenImplementationReturnsValue() {
        Code code = () -> "<style>.x{}</style>";

        assertEquals("<style>.x{}</style>", code.raw());
    }

    @Test
    void shouldExposeNullRawValue_whenImplementationReturnsNull() {
        Code code = () -> null;

        assertNull(code.raw());
    }
}

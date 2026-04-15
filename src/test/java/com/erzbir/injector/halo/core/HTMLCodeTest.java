package com.erzbir.injector.halo.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HTMLCodeTest {

    @Test
    void shouldExposeRawCode_whenConstructedWithNormalValue() {
        HTMLCode htmlCode = new HTMLCode("<script>alert('x')</script>");

        assertEquals("<script>alert('x')</script>", htmlCode.raw());
    }

    @Test
    void shouldExposeEmptyRawCode_whenConstructedWithEmptyValue() {
        HTMLCode htmlCode = new HTMLCode("");

        assertEquals("", htmlCode.raw());
    }

    @Test
    void shouldExposeNullRawCode_whenConstructedWithNullValue() {
        HTMLCode htmlCode = new HTMLCode(null);

        assertNull(htmlCode.raw());
    }
}

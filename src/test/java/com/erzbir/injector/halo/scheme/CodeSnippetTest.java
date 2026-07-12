package com.erzbir.injector.halo.scheme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeSnippetTest {

    @Test
    void shouldBeInvalidWhenCodeIsNullOrBlank() {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setCode(null);

        assertFalse(snippet.valid());

        snippet.setCode(" ");

        assertFalse(snippet.valid());
    }

    @Test
    void shouldBeValidWhenCodeContainsText() {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("<script>console.log('ok')</script>");

        assertTrue(snippet.valid());
    }
}

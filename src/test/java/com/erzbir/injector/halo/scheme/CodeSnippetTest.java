package com.erzbir.injector.halo.scheme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeSnippetTest {

    @Test
    void shouldBeInvalidWhenCodeIsNullOrBlank() {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setCode(null);

        assertFalse(snippet.isValid());

        snippet.setCode(" ");

        assertFalse(snippet.isValid());
    }

    @Test
    void shouldBeValidWhenCodeContainsText() {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setCode("<script>console.log('ok')</script>");

        assertTrue(snippet.isValid());
    }
}

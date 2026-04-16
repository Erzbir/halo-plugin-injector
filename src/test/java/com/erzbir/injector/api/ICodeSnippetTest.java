package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ICodeSnippetTest {

    @Test
    void shouldExposeAllValues_whenImplementationReturnsRegularValues() {
        Set<String> ruleIds = Set.of("rule-1", "rule-2");
        ICodeSnippet snippet = new FixedCodeSnippet("id-1", "name", "desc", "<script>x</script>", true, ruleIds);

        assertEquals("id-1", snippet.getId());
        assertEquals("name", snippet.getName());
        assertEquals("desc", snippet.getDescription());
        assertEquals("<script>x</script>", snippet.getCode());
        assertTrue(snippet.isEnabled());
        assertSame(ruleIds, snippet.getRuleIds());
    }

    @Test
    void shouldExposeNullAndEmptyValues_whenImplementationReturnsBoundaryValues() {
        ICodeSnippet snippet = new FixedCodeSnippet(null, "", null, "", false, Collections.emptySet());

        assertNull(snippet.getId());
        assertEquals("", snippet.getName());
        assertNull(snippet.getDescription());
        assertEquals("", snippet.getCode());
        assertFalse(snippet.isEnabled());
        assertTrue(snippet.getRuleIds().isEmpty());
    }

    private record FixedCodeSnippet(String id,
                                    String name,
                                    String description,
                                    String code,
                                    boolean enabled,
                                    Set<String> ruleIds) implements ICodeSnippet {
        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public Set<String> getRuleIds() {
            return ruleIds;
        }
    }
}

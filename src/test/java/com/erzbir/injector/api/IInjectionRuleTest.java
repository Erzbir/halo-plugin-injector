package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IInjectionRuleTest {

    @Test
    void shouldExposeAllValues_whenImplementationReturnsRegularValues() {
        Set<String> snippetIds = Set.of("snippet-1", "snippet-2");
        MatchRule matchRule = MatchRule.pathRule(MatchRule.Matcher.PATH_PATTERN, "/**");
        IInjectionRule rule = new FixedInjectionRule(
                "rule-1",
                "name",
                "desc",
                true,
                InjectMode.HEAD,
                "head",
                InjectPosition.APPEND,
                snippetIds,
                matchRule
        );

        assertEquals("rule-1", rule.getId());
        assertEquals("name", rule.getName());
        assertEquals("desc", rule.getDescription());
        assertTrue(rule.isEnabled());
        assertEquals(InjectMode.HEAD, rule.getMode());
        assertEquals("head", rule.getMatch());
        assertEquals(InjectPosition.APPEND, rule.getPosition());
        assertSame(snippetIds, rule.getSnippetIds());
        assertSame(matchRule, rule.getMatchRule());
    }

    @Test
    void shouldExposeNullAndEmptyValues_whenImplementationReturnsBoundaryValues() {
        IInjectionRule rule = new FixedInjectionRule(
                null,
                "",
                null,
                false,
                null,
                "",
                null,
                Collections.emptySet(),
                null
        );

        assertNull(rule.getId());
        assertEquals("", rule.getName());
        assertNull(rule.getDescription());
        assertFalse(rule.isEnabled());
        assertNull(rule.getMode());
        assertEquals("", rule.getMatch());
        assertNull(rule.getPosition());
        assertTrue(rule.getSnippetIds().isEmpty());
        assertNull(rule.getMatchRule());
    }

    private record FixedInjectionRule(String id,
                                      String name,
                                      String description,
                                      boolean enabled,
                                      InjectMode mode,
                                      String match,
                                      InjectPosition position,
                                      Set<String> snippetIds,
                                      MatchRule matchRule) implements IInjectionRule {
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
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public InjectMode getMode() {
            return mode;
        }

        @Override
        public String getMatch() {
            return match;
        }

        @Override
        public InjectPosition getPosition() {
            return position;
        }

        @Override
        public Set<String> getSnippetIds() {
            return snippetIds;
        }

        @Override
        public MatchRule getMatchRule() {
            return matchRule;
        }
    }
}

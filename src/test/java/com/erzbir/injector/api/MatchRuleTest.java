package com.erzbir.injector.api;

import com.erzbir.injector.halo.scheme.MatchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchRuleTest {

    @Test
    void shouldCreateDefaultPathRuleWithoutChildren() {
        MatchRule rule = MatchRule.defaultRule();

        assertEquals(MatchRuleType.PATH, rule.getType());
        assertEquals(MatcherType.PATH_PATTERN, rule.getMatcher());
        assertEquals("/**", rule.getValue());
        assertNotNull(rule.getChildren());
        assertTrue(rule.getChildren().isEmpty());
        assertTrue(rule.valid());
    }

    @Test
    void shouldFallbackToAndWhenGroupOperatorIsNull() {
        MatchRule rule = MatchRule.groupRule(null, List.of());

        assertEquals(Operator.AND, rule.getOperator());
    }

    @Test
    void shouldFallbackToAndWhenPathOperatorIsNull() {
        MatchRule rule = MatchRule.pathRule(null, MatcherType.EXACT, "/a");

        assertEquals(Operator.AND, rule.getOperator());
    }

    @Test
    void shouldAddChildWhenChildrenIsNull() {
        MatchRule parent = new MatchRule();
        parent.setType(MatchRuleType.GROUP);
        parent.setChildren(null);
        MatchRule child = MatchRule.pathRule(MatcherType.EXACT, "/a");

        parent.addChild(child);

        assertNotNull(parent.getChildren());
        assertEquals(1, parent.getChildren().size());
        assertEquals(child, parent.getChildren().getFirst());
    }

    @Test
    void shouldBeInvalidWhenTypeIsNull() {
        MatchRule rule = new MatchRule();
        rule.setType(null);

        assertFalse(rule.valid());
    }

    @Test
    void shouldBeInvalidWhenGroupHasEmptyChildren() {
        MatchRule rule = MatchRule.groupRule(Operator.AND);

        assertFalse(rule.valid());
    }

    @Test
    void shouldBeInvalidWhenGroupHasInvalidChild() {
        MatchRule validChild = MatchRule.pathRule(MatcherType.EXACT, "/a");
        MatchRule invalidChild = MatchRule.pathRule(MatcherType.EXACT, " ");
        MatchRule parent = MatchRule.groupRule(Operator.AND, validChild, invalidChild);

        assertFalse(parent.valid());
    }

    @Test
    void shouldBeInvalidWhenRegexIsMalformed() {
        MatchRule rule = MatchRule.pathRule(MatcherType.REGEX, "*bad");

        assertFalse(rule.valid());
    }
}

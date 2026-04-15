package com.erzbir.injector.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchRuleTest {

    @Test
    void shouldCreateValidDefaultRule() {
        MatchRule rule = MatchRule.defaultRule();

        assertEquals(MatchRule.Type.GROUP, rule.getType());
        assertEquals(1, rule.getChildren().size());
        MatchRule child = rule.getChildren().getFirst();
        assertEquals(MatchRule.Matcher.PATH_PATTERN, child.getMatcher());
        assertEquals("/**", child.getValue());
        assertTrue(rule.isValid());
    }

    @Test
    void shouldFallbackToAndWhenGroupOperatorIsNull() {
        MatchRule rule = MatchRule.groupRule(null, List.of());

        assertEquals(MatchRule.Operator.AND, rule.getOperator());
    }

    @Test
    void shouldFallbackToAndWhenPathOperatorIsNull() {
        MatchRule rule = MatchRule.pathRule(null, MatchRule.Matcher.EXACT, "/a");

        assertEquals(MatchRule.Operator.AND, rule.getOperator());
    }

    @Test
    void shouldAddChildWhenChildrenIsNull() {
        MatchRule parent = new MatchRule();
        parent.setType(MatchRule.Type.GROUP);
        parent.setChildren(null);
        MatchRule child = MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a");

        parent.addChild(child);

        assertNotNull(parent.getChildren());
        assertEquals(1, parent.getChildren().size());
        assertEquals(child, parent.getChildren().getFirst());
    }

    @Test
    void shouldBeInvalidWhenTypeIsNull() {
        MatchRule rule = new MatchRule();
        rule.setType(null);

        assertFalse(rule.isValid());
    }

    @Test
    void shouldBeInvalidWhenGroupHasEmptyChildren() {
        MatchRule rule = MatchRule.groupRule(MatchRule.Operator.AND);

        assertFalse(rule.isValid());
    }

    @Test
    void shouldBeInvalidWhenGroupHasInvalidChild() {
        MatchRule validChild = MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a");
        MatchRule invalidChild = MatchRule.pathRule(MatchRule.Matcher.EXACT, " ");
        MatchRule parent = MatchRule.groupRule(MatchRule.Operator.AND, validChild, invalidChild);

        assertFalse(parent.isValid());
    }

    @Test
    void shouldBeInvalidWhenRegexIsMalformed() {
        MatchRule rule = MatchRule.pathRule(MatchRule.Matcher.REGEX, "*bad");

        assertFalse(rule.isValid());
    }
}

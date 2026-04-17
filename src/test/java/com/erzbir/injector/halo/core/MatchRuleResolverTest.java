package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.MatcherType;
import com.erzbir.injector.api.Operator;
import com.erzbir.injector.halo.scheme.MatchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchRuleResolverTest {
    private MatchRuleResolver evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new MatchRuleResolver();
    }

    @Test
    void shouldMatchAndGroup() {
        MatchRule root = MatchRule.groupRule(
                Operator.AND,
                MatchRule.pathRule(MatcherType.ANT, "/posts/**"),
                MatchRule.pathRule(MatcherType.REGEX, "^/posts/[0-9]+$")
        );

        assertTrue(evaluator.matches(root, "/posts/123"));
        assertFalse(evaluator.matches(root, "/posts/test"));
    }

    @Test
    void shouldMatchOrGroup() {
        MatchRule root = MatchRule.groupRule(
                Operator.OR,
                MatchRule.pathRule(MatcherType.EXACT, "/a"),
                MatchRule.pathRule(Operator.OR, MatcherType.EXACT, "/b")
        );

        assertTrue(evaluator.matches(root, "/a"));
        assertTrue(evaluator.matches(root, "/b"));
        assertFalse(evaluator.matches(root, "/c"));
    }

    @Test
    void shouldApplyNotOperatorOnLeafAndGroup() {
        MatchRule negatedLeaf = MatchRule.pathRule(Operator.NOT, MatcherType.EXACT, "/admin");

        assertTrue(evaluator.matches(negatedLeaf, "/home"));
        assertFalse(evaluator.matches(negatedLeaf, "/admin"));

        MatchRule group = MatchRule.groupRule(
                Operator.AND,
                MatchRule.pathRule(Operator.AND, MatcherType.EXACT, "/a"),
                MatchRule.pathRule(Operator.NOT, MatcherType.EXACT, "/b")
        );

        assertTrue(evaluator.matches(group, "/a"));
        assertFalse(evaluator.matches(group, "/b"));
        assertFalse(evaluator.matches(group, "/c"));

    }

    @Test
    void shouldHandleInvalidInputAsNotMatched() {
        assertFalse(evaluator.matches(null, "/a"));
        assertFalse(evaluator.matches(MatchRule.defaultRule(), null));
        assertFalse(evaluator.matches(MatchRule.defaultRule(), " "));
    }

    @Test
    void shouldReturnFalseForInvalidRegexPattern() {
        MatchRule invalidRegex = MatchRule.pathRule(MatcherType.REGEX, "*invalid");

        assertFalse(evaluator.matches(invalidRegex, "/posts/1"));
    }

    @Test
    void shouldSupportPathPatternMatcher() {
        MatchRule pathPattern = MatchRule.pathRule(MatcherType.PATH_PATTERN, "/posts/{id}");

        assertTrue(evaluator.matches(pathPattern, "/posts/1"));
        assertFalse(evaluator.matches(pathPattern, "/posts/1/2"));
    }

    @Test
    void shouldApplyNotOnPathRule() {
        MatchRule notExact = MatchRule.pathRule(Operator.NOT, MatcherType.EXACT, "/admin");

        assertTrue(evaluator.matches(notExact, "/home"));
        assertFalse(evaluator.matches(notExact, "/admin"));
    }

    @Test
    void shouldSupportNotOperatorInGroupChain() {
        MatchRule second = MatchRule.pathRule(Operator.NOT, MatcherType.EXACT, "/b");
        MatchRule group = MatchRule.groupRule(
                Operator.AND,
                MatchRule.pathRule(Operator.AND, MatcherType.EXACT, "/a"),
                second
        );

        assertTrue(evaluator.matches(group, "/a"));
        assertFalse(evaluator.matches(group, "/b"));
        assertFalse(evaluator.matches(group, "/c"));
    }
}

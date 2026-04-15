package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.MatchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchRuleEvaluatorTest {
    private MatchRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new MatchRuleEvaluator();
    }

    @Test
    void shouldMatchAndGroup() {
        MatchRule root = MatchRule.groupRule(
                MatchRule.Operator.AND,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.pathRule(MatchRule.Matcher.REGEX, "^/posts/[0-9]+$")
        );

        assertTrue(evaluator.matches(root, "/posts/123"));
        assertFalse(evaluator.matches(root, "/posts/test"));
    }

    @Test
    void shouldMatchOrGroup() {
        MatchRule root = MatchRule.groupRule(
                MatchRule.Operator.OR,
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a"),
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/b")
        );

        assertTrue(evaluator.matches(root, "/a"));
        assertTrue(evaluator.matches(root, "/b"));
        assertFalse(evaluator.matches(root, "/c"));
    }

    @Test
    void shouldApplyNotOperatorOnLeafAndGroup() {
        MatchRule negatedLeaf = MatchRule.pathRule(MatchRule.Operator.NOT, MatchRule.Matcher.EXACT, "/admin");

        assertTrue(evaluator.matches(negatedLeaf, "/home"));
        assertFalse(evaluator.matches(negatedLeaf, "/admin"));

        MatchRule group = MatchRule.groupRule(
                MatchRule.Operator.NOT,
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a"),
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/b")
        );

        assertTrue(evaluator.matches(group, "/c"));
        assertFalse(evaluator.matches(group, "/a"));
    }

    @Test
    void shouldHandleInvalidInputAsNotMatched() {
        assertFalse(evaluator.matches(null, "/a"));
        assertFalse(evaluator.matches(MatchRule.defaultRule(), null));
        assertFalse(evaluator.matches(MatchRule.defaultRule(), " "));
    }

    @Test
    void shouldReturnFalseForInvalidRegexPattern() {
        MatchRule invalidRegex = MatchRule.pathRule(MatchRule.Matcher.REGEX, "*invalid");

        assertFalse(evaluator.matches(invalidRegex, "/posts/1"));
    }

    @Test
    void shouldSupportPathPatternMatcher() {
        MatchRule pathPattern = MatchRule.pathRule(MatchRule.Matcher.PATH_PATTERN, "/posts/{id}");

        assertTrue(evaluator.matches(pathPattern, "/posts/1"));
        assertFalse(evaluator.matches(pathPattern, "/posts/1/2"));
    }

    @Test
    void shouldApplyNotOnPathRule() {
        MatchRule notExact = MatchRule.pathRule(MatchRule.Operator.NOT, MatchRule.Matcher.EXACT, "/admin");

        assertTrue(evaluator.matches(notExact, "/home"));
        assertFalse(evaluator.matches(notExact, "/admin"));
    }
}

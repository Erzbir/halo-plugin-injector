package com.erzbir.halo.injector.util;

import com.erzbir.injector.api.MatchRule;
import com.erzbir.injector.halo.util.MatchRuleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.PathContainer;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchRuleEvaluatorTest {
    private MatchRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        PathPatternParser parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        RouteMatcher matcher = new PathPatternRouteMatcher(parser);
        evaluator = new MatchRuleEvaluator(matcher);
    }

    @Test
    void shouldMatchAndGroup() {
        MatchRule root = MatchRule.groupRule(
                MatchRule.Operator.AND,
                MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"),
                MatchRule.pathRule(MatchRule.Matcher.REGEX, "^/posts/[0-9]+$")
        );

        assertTrue(evaluator.matchesPath(root, "/posts/123"));
        assertFalse(evaluator.matchesPath(root, "/posts/test"));
    }

    @Test
    void shouldMatchOrGroup() {
        MatchRule root = MatchRule.groupRule(
                MatchRule.Operator.OR,
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a"),
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/b")
        );

        assertTrue(evaluator.matchesPath(root, "/a"));
        assertTrue(evaluator.matchesPath(root, "/b"));
        assertFalse(evaluator.matchesPath(root, "/c"));
    }

    @Test
    void shouldApplyNotOperatorOnLeafAndGroup() {
        MatchRule negatedLeaf = MatchRule.pathRule(MatchRule.Operator.NOT, MatchRule.Matcher.EXACT, "/admin");

        assertTrue(evaluator.matchesPath(negatedLeaf, "/home"));
        assertFalse(evaluator.matchesPath(negatedLeaf, "/admin"));

        MatchRule group = MatchRule.groupRule(
                MatchRule.Operator.NOT,
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a"),
                MatchRule.pathRule(MatchRule.Matcher.EXACT, "/b")
        );

        assertTrue(evaluator.matchesPath(group, "/c"));
        assertFalse(evaluator.matchesPath(group, "/a"));
    }
}

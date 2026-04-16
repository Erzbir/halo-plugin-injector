package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.core.MatchRule;
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
        MatchRule root = new MatchRule();
        root.setType(MatchRule.Type.GROUP);
        root.setOperator(MatchRule.Operator.AND);
        root.setNegate(false);
        root.getChildren().add(MatchRule.pathRule(MatchRule.Matcher.ANT, "/posts/**"));
        root.getChildren().add(MatchRule.pathRule(MatchRule.Matcher.REGEX, "^/posts/[0-9]+$"));

        assertTrue(evaluator.matchesPath(root, "/posts/123"));
        assertFalse(evaluator.matchesPath(root, "/posts/test"));
    }

    @Test
    void shouldMatchOrGroup() {
        MatchRule root = new MatchRule();
        root.setType(MatchRule.Type.GROUP);
        root.setOperator(MatchRule.Operator.OR);
        root.setNegate(false);
        root.getChildren().add(MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a"));
        root.getChildren().add(MatchRule.pathRule(MatchRule.Matcher.EXACT, "/b"));

        assertTrue(evaluator.matchesPath(root, "/a"));
        assertTrue(evaluator.matchesPath(root, "/b"));
        assertFalse(evaluator.matchesPath(root, "/c"));
    }

    @Test
    void shouldApplyNotOperatorOnLeafAndGroup() {
        MatchRule negatedLeaf = MatchRule.pathRule(MatchRule.Matcher.EXACT, "/admin");
        negatedLeaf.setNegate(true);

        assertTrue(evaluator.matchesPath(negatedLeaf, "/home"));
        assertFalse(evaluator.matchesPath(negatedLeaf, "/admin"));

        MatchRule group = new MatchRule();
        group.setType(MatchRule.Type.GROUP);
        group.setOperator(MatchRule.Operator.OR);
        group.setNegate(true);
        group.getChildren().add(MatchRule.pathRule(MatchRule.Matcher.EXACT, "/a"));
        group.getChildren().add(MatchRule.pathRule(MatchRule.Matcher.EXACT, "/b"));

        assertTrue(evaluator.matchesPath(group, "/c"));
        assertFalse(evaluator.matchesPath(group, "/a"));
    }
}


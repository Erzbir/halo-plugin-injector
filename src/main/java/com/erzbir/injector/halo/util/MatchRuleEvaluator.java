package com.erzbir.injector.halo.util;

import com.erzbir.injector.api.MatchRule;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class MatchRuleEvaluator {
    private final RouteMatcher routeMatcher;

    public MatchRuleEvaluator(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    public boolean matchesPath(MatchRule rule, String path) {
        return matches(rule, path);
    }

    public boolean matches(MatchRule rule, String path) {
        if (rule == null || path == null || path.isBlank()) {
            return false;
        }
        var requestRoute = routeMatcher.parseRoute(path);
        return evaluate(rule, path, requestRoute);
    }

    private boolean evaluate(MatchRule rule,
                             String path,
                             RouteMatcher.Route requestRoute) {
        if (rule == null || rule.getType() == null) {
            return false;
        }
        return switch (rule.getType()) {
            case GROUP -> evaluateGroup(rule, path, requestRoute);
            case PATH -> evaluatePath(rule, path, requestRoute);
        };
    }

    private boolean evaluateGroup(MatchRule rule,
                                  String path,
                                  RouteMatcher.Route requestRoute) {
        List<MatchRule> children = rule.getChildren();
        if (children == null || children.isEmpty()) {
            return false;
        }
        MatchRule.Operator operator = rule.getOperator();
        return switch (operator) {
            case AND -> children.stream().allMatch(child -> evaluate(child, path, requestRoute));
            case OR -> children.stream().anyMatch(child -> evaluate(child, path, requestRoute));
            case NOT -> children.stream().noneMatch(child -> evaluate(child, path, requestRoute));
        };
    }

    private boolean evaluatePath(MatchRule rule, String path, RouteMatcher.Route requestRoute) {
        if (rule.getMatcher() == null || rule.getValue() == null || rule.getValue().isBlank()) {
            return false;
        }
        boolean result = switch (rule.getMatcher()) {
            case ANT -> matchAnt(rule.getValue(), requestRoute);
            case EXACT -> Objects.equals(rule.getValue(), path);
            case REGEX -> Pattern.compile(rule.getValue()).matcher(path).matches();
        };
        MatchRule.Operator operator = rule.getOperator();
        return switch (operator) {
            case NOT -> !result;
            case AND, OR -> result;
        };
    }

    private boolean matchAnt(String pattern, RouteMatcher.Route requestRoute) {
        try {
            return routeMatcher.match(pattern, requestRoute);
        } catch (PatternParseException e) {
            return false;
        }
    }
}

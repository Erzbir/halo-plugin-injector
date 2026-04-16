package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.core.MatchRule;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MatchRuleEvaluator {
    private final RouteMatcher routeMatcher;

    public MatchRuleEvaluator(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    public boolean matchesPath(MatchRule rule, String path) {
        return matches(rule, path, null);
    }

    public boolean matches(MatchRule rule, String path, String templateId) {
        if (rule == null || path == null || path.isBlank()) {
            return false;
        }
        var requestRoute = routeMatcher.parseRoute(path);
        return evaluate(rule, path, templateId, requestRoute);
    }

    private boolean evaluate(MatchRule rule,
                             String path,
                             String templateId,
                             RouteMatcher.Route requestRoute) {
        if (rule == null || rule.getType() == null) {
            return false;
        }
        boolean base = switch (rule.getType()) {
            case GROUP -> evaluateGroup(rule, path, templateId, requestRoute);
            case PATH -> evaluatePath(rule, path, requestRoute);
        };
        return Boolean.TRUE.equals(rule.getNegate()) != base;
    }

    private boolean evaluateGroup(MatchRule rule,
                                  String path,
                                  String templateId,
                                  RouteMatcher.Route requestRoute) {
        List<MatchRule> children = rule.getChildren();
        if (children == null || children.isEmpty()) {
            return false;
        }
        MatchRule.Operator operator = rule.getOperator() == null ? MatchRule.Operator.AND : rule.getOperator();
        return switch (operator) {
            case AND -> children.stream().allMatch(child -> evaluate(child, path, templateId, requestRoute));
            case OR -> children.stream().anyMatch(child -> evaluate(child, path, templateId, requestRoute));
        };
    }

    private boolean evaluatePath(MatchRule rule, String path, RouteMatcher.Route requestRoute) {
        if (rule.getMatcher() == null || rule.getValue() == null || rule.getValue().isBlank()) {
            return false;
        }
        return switch (rule.getMatcher()) {
            case ANT -> matchAnt(rule.getValue(), requestRoute, path);
            case EXACT -> Objects.equals(rule.getValue(), path);
            case REGEX -> Pattern.compile(rule.getValue()).matcher(path).matches();
        };
    }

    private boolean matchAnt(String pattern, RouteMatcher.Route requestRoute, String path) {
        try {
            return routeMatcher.match(pattern, requestRoute);
        } catch (PatternParseException e) {
            return false;
        }
    }
}


package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.MatchRule;
import org.springframework.http.server.PathContainer;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.SimpleRouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class MatchRuleEvaluator {

    private final SimpleRouteMatcher antMatcher;
    private final PathPatternRouteMatcher patternMatcher;
    private final ConcurrentHashMap<String, Pattern> regexCache = new ConcurrentHashMap<>();

    public MatchRuleEvaluator() {
        this.antMatcher = new SimpleRouteMatcher(new AntPathMatcher());

        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        this.patternMatcher = new PathPatternRouteMatcher(parser);
    }

    public boolean matches(MatchRule rule, String path) {
        if (rule == null || path == null || path.isBlank()) {
            return false;
        }
        return evaluate(rule, path);
    }

    private boolean evaluate(MatchRule rule, String path) {
        if (rule == null || rule.getType() == null) {
            return false;
        }
        return switch (rule.getType()) {
            case GROUP -> evaluateGroup(rule, path);
            case PATH -> evaluatePath(rule, path);
        };
    }

    private boolean evaluateGroup(MatchRule rule, String path) {
        List<MatchRule> children = rule.getChildren();
        if (children == null || children.isEmpty()) {
            return false;
        }
        return switch (rule.getOperator()) {
            case AND -> children.stream().allMatch(c -> evaluate(c, path));
            case OR -> children.stream().anyMatch(c -> evaluate(c, path));
            case NOT -> children.stream().noneMatch(c -> evaluate(c, path));
        };
    }

    private boolean evaluatePath(MatchRule rule, String path) {
        if (rule.getMatcher() == null || isBlank(rule.getValue())) {
            return false;
        }
        boolean matched = switch (rule.getMatcher()) {
            case PATH_PATTERN -> matchPattern(rule.getValue(), path);
            case ANT -> matchAnt(rule.getValue(), path);
            case EXACT -> Objects.equals(rule.getValue(), path);
            case REGEX -> matchRegex(rule.getValue(), path);
        };
        return (rule.getOperator() == MatchRule.Operator.NOT) != matched;
    }

    private boolean matchAnt(String pattern, String path) {
        try {
            return antMatcher.match(pattern, antMatcher.parseRoute(path));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchRegex(String pattern, String path) {
        try {
            return regexCache
                    .computeIfAbsent(pattern, Pattern::compile)
                    .matcher(path)
                    .matches();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private boolean matchPattern(String pattern, String path) {
        try {
            return patternMatcher.match(pattern, patternMatcher.parseRoute(path));
        } catch (PatternParseException e) {
            return false;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

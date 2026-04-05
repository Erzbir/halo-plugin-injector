package com.erzbir.halo.injector.scheme;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Data
public class MatchRule {
    private Type type = Type.GROUP;
    private Boolean negate = false;
    private Operator operator = Operator.AND;
    private Matcher matcher = Matcher.ANT;
    private String value = "";
    private List<MatchRule> children = new ArrayList<>();

    public static MatchRule defaultRule() {
        MatchRule root = new MatchRule();
        root.setType(Type.GROUP);
        root.setOperator(Operator.AND);
        root.getChildren().add(pathRule(Matcher.ANT, "/**"));
        return root;
    }

    public static MatchRule pathRule(Matcher matcher, String value) {
        MatchRule rule = new MatchRule();
        rule.setType(Type.PATH);
        rule.setMatcher(matcher);
        rule.setValue(value);
        return rule;
    }

    public static MatchRule templateRule(Matcher matcher, String value) {
        MatchRule rule = new MatchRule();
        rule.setType(Type.TEMPLATE_ID);
        rule.setMatcher(matcher);
        rule.setValue(value);
        return rule;
    }

    public boolean isValid() {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case GROUP -> children != null
                    && !children.isEmpty()
                    && children.stream().allMatch(child -> child != null && child.isValid());
            case PATH -> supportsPathMatcher(matcher)
                    && StringUtils.hasText(value)
                    && regexValidIfNeeded();
            case TEMPLATE_ID -> supportsTemplateMatcher(matcher)
                    && StringUtils.hasText(value)
                    && regexValidIfNeeded();
        };
    }

    private boolean regexValidIfNeeded() {
        if (matcher != Matcher.REGEX) {
            return true;
        }
        try {
            // 后端再次校验，避免非法正则绕过前端后落库，导致运行时才以“不生效”形式暴露问题。
            Pattern.compile(value);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private boolean supportsPathMatcher(Matcher matcher) {
        return matcher == Matcher.ANT || matcher == Matcher.REGEX || matcher == Matcher.EXACT;
    }

    private boolean supportsTemplateMatcher(Matcher matcher) {
        return matcher == Matcher.EXACT || matcher == Matcher.REGEX;
    }

    public enum Type {
        GROUP, PATH, TEMPLATE_ID
    }

    public enum Operator {
        AND, OR
    }

    public enum Matcher {
        ANT, REGEX, EXACT
    }
}

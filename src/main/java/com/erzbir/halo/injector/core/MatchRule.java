package com.erzbir.halo.injector.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Data
public class MatchRule {
    private Type type = Type.GROUP;
    private Boolean negate;
    private Operator operator;
    private Matcher matcher;
    private String value;
    private List<MatchRule> children = new ArrayList<>();

    public static MatchRule defaultRule() {
        MatchRule root = new MatchRule();
        root.setNegate(false);
        root.setOperator(Operator.AND);
        root.getChildren().add(pathRule(Matcher.ANT, "/**"));
        return root;
    }

    public static MatchRule pathRule(Matcher matcher, String value) {
        MatchRule rule = new MatchRule();
        rule.setType(Type.PATH);
        rule.setNegate(false);
        rule.setMatcher(matcher);
        rule.setValue(value);
        return rule;
    }

    @JsonIgnore
    public boolean isValid() {
        if (type == null) {
            return false;
        }
        if (Matcher.REGEX.equals(matcher)) {
            try {
                Pattern.compile(value);
            } catch (Throwable e) {
                return false;
            }
        }
        return switch (type) {
            case GROUP -> children != null
                    && !children.isEmpty()
                    && (operator == Operator.AND || operator == Operator.OR)
                    && children.stream().allMatch(child -> child != null && child.isValid());
            case PATH -> supportsPathMatcher(matcher) && StringUtils.hasText(value);
        };
    }

    private boolean supportsPathMatcher(Matcher matcher) {
        return matcher == Matcher.ANT || matcher == Matcher.REGEX || matcher == Matcher.EXACT;
    }

    public enum Type {
        GROUP, PATH
    }

    public enum Operator {
        AND, OR
    }

    public enum Matcher {
        ANT, REGEX, EXACT
    }
}

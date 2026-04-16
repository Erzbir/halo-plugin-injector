package com.erzbir.injector.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Data
public class MatchRule {
    private Type type = Type.GROUP;
    private Operator operator = Operator.AND;
    private Matcher matcher;
    private String value = "";
    private List<MatchRule> children = new ArrayList<>();

    public static MatchRule defaultRule() {
        return groupRule(Operator.AND, pathRule(Matcher.PATH_PATTERN, "/**"));
    }

    public static MatchRule groupRule(Operator operator, MatchRule... children) {
        return groupRule(operator, Arrays.asList(children));
    }

    public static MatchRule groupRule(Operator operator, List<MatchRule> children) {
        MatchRule rule = new MatchRule();
        rule.setType(Type.GROUP);
        rule.setOperator(operator == null ? Operator.AND : operator);
        rule.setChildren(new ArrayList<>(children == null ? List.of() : children));
        return rule;
    }

    public static MatchRule pathRule(Matcher matcher, String value) {
        return pathRule(Operator.AND, matcher, value);
    }

    public static MatchRule pathRule(Operator operator, Matcher matcher, String value) {
        MatchRule rule = new MatchRule();
        rule.setType(Type.PATH);
        rule.setOperator(operator == null ? Operator.AND : operator);
        rule.setMatcher(matcher);
        rule.setValue(value);
        return rule;
    }

    public MatchRule addChild(MatchRule child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return this;
    }

    @JsonIgnore
    public boolean isValid() {
        if (type == null) {
            return false;
        }
        if (Matcher.REGEX.equals(matcher)) {
            try {
                Pattern.compile(value);
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
        return switch (type) {
            case GROUP -> children != null
                    && !children.isEmpty()
                    && children.stream().allMatch(child -> child != null && child.isValid());
            case PATH -> StringUtils.hasText(value);
        };
    }

    public enum Type {
        GROUP, PATH
    }

    public enum Operator {
        AND, OR, NOT
    }

    public enum Matcher {
        ANT, REGEX, EXACT, PATH_PATTERN
    }
}

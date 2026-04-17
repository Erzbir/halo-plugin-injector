package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.IMatchRule;
import com.erzbir.injector.api.MatchRuleType;
import com.erzbir.injector.api.MatcherType;
import com.erzbir.injector.api.Operator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
public class MatchRule implements IMatchRule {
    @NotNull(message = "MatchRule type must not be null")
    private MatchRuleType type = MatchRuleType.GROUP;
    @NotNull(message = "MatchRule operator must not be null")
    private Operator operator = Operator.AND;
    @NotNull(message = "MatchRule matcher must not be null")
    private MatcherType matcher = MatcherType.PATH_PATTERN;
    private String value = "";
    @Valid
    private List<MatchRule> children = new ArrayList<>();

    public static MatchRule defaultRule() {
        return pathRule(MatcherType.PATH_PATTERN, "/**");
    }

    public static MatchRule groupRule(Operator operator, MatchRule... children) {
        return groupRule(operator, Arrays.asList(children));
    }

    public static MatchRule groupRule(Operator operator, List<MatchRule> children) {
        MatchRule rule = new MatchRule();
        rule.setType(MatchRuleType.GROUP);
        rule.setOperator(operator == null ? Operator.AND : operator);
        rule.setChildren(new ArrayList<>(children == null ? List.of() : children));
        return rule;
    }

    public static MatchRule pathRule(MatcherType matcher, String value) {
        return pathRule(Operator.AND, matcher, value);
    }

    public static MatchRule pathRule(Operator operator, MatcherType matcher, String value) {
        MatchRule rule = new MatchRule();
        rule.setType(MatchRuleType.PATH);
        rule.setOperator(operator == null ? Operator.AND : operator);
        rule.setMatcher(matcher == null ? MatcherType.PATH_PATTERN : matcher);
        rule.setValue(value == null ? "" : value);
        return rule;
    }

    public void addChild(MatchRule child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public boolean valid() {
        if (type == null || operator == null) {
            return false;
        }
        return switch (type) {
            case GROUP -> children != null
                    && !children.isEmpty()
                    && children.stream().allMatch(child -> child != null && child.valid());
            case PATH -> {
                if (matcher == null || value == null || value.isBlank()) {
                    yield false;
                }
                if (MatcherType.REGEX.equals(matcher)) {
                    try {
                        Pattern.compile(value);
                    } catch (PatternSyntaxException e) {
                        yield false;
                    }
                }
                yield true;
            }
        };
    }

    @AssertTrue(message = "MatchRule is invalid")
    @SuppressWarnings("unused")
    private boolean isMatchRuleValid() {
        return valid();
    }

}

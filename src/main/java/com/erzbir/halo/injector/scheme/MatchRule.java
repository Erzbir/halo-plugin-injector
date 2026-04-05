package com.erzbir.halo.injector.scheme;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
        // 这里只保留运行时快速判定，避免每次请求都重复编译正则；
        // 非法正则由写入期校验兜底，防止把坏数据持久化进去。
        return switch (type) {
            case GROUP -> children != null
                    && !children.isEmpty()
                    && children.stream().allMatch(child -> child != null && child.isValid());
            case PATH -> supportsPathMatcher(matcher) && StringUtils.hasText(value);
            case TEMPLATE_ID -> supportsTemplateMatcher(matcher) && StringUtils.hasText(value);
        };
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

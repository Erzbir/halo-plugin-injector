package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.IMatchRule;
import com.erzbir.injector.api.MatchRuleType;
import com.erzbir.injector.api.Operator;
import lombok.Getter;

import java.util.EnumMap;
import java.util.List;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Getter
enum MatchRuleEvaluators {
    GROUP(RecursiveRuleEvaluator.INSTANCE),
    PATH(PathRuleEvaluator.INSTANCE);

    private final RuleEvaluator evaluator;

    MatchRuleEvaluators(RuleEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public static RuleEvaluator resolve(MatchRuleType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case GROUP -> GROUP.evaluator;
            case PATH -> PATH.evaluator;
        };
    }
}

final class RecursiveRuleEvaluator implements RuleEvaluator {
    public static final RecursiveRuleEvaluator INSTANCE = new RecursiveRuleEvaluator();

    private final EnumMap<MatchRuleType, RuleEvaluator> evaluators = new EnumMap<>(MatchRuleType.class);

    private RecursiveRuleEvaluator() {
        evaluators.put(MatchRuleType.GROUP, new GroupRuleEvaluator(this));
        evaluators.put(MatchRuleType.PATH, PathRuleEvaluator.INSTANCE);
    }

    @Override
    public boolean evaluate(IMatchRule rule, String path) {
        if (rule == null || rule.getType() == null) {
            return false;
        }
        RuleEvaluator evaluator = evaluators.get(rule.getType());
        return evaluator != null && evaluator.evaluate(rule, path);
    }
}

final class GroupRuleEvaluator implements RuleEvaluator {
    private final RuleEvaluator recursiveEvaluator;

    public GroupRuleEvaluator(RuleEvaluator recursiveEvaluator) {
        this.recursiveEvaluator = recursiveEvaluator;
    }

    @Override
    public boolean evaluate(IMatchRule rule, String path) {
        return evaluateGroup(rule, path);
    }

    private boolean evaluateGroup(IMatchRule rule, String path) {
        List<? extends IMatchRule> children = rule.getChildren();
        if (children == null || children.isEmpty()) {
            return false;
        }
        Boolean result = null;
        for (IMatchRule child : children) {
            Operator operator = child.getOperator() == null ? Operator.AND : child.getOperator();
            boolean matched = recursiveEvaluator.evaluate(child, path);
            if (operator.isNegated()) {
                matched = !matched;
            }
            if (result == null) {
                result = matched;
                continue;
            }
            result = merge(result, matched, operator.getConnector());
        }
        return result;
    }

    private boolean merge(Boolean previous, boolean current, Operator operator) {
        if (previous == null) {
            return current;
        }
        return switch (operator) {
            case AND, NOT, AND_NOT, OR_NOT -> previous && current;
            case OR -> previous || current;
        };
    }
}

final class PathRuleEvaluator implements RuleEvaluator {
    public static final PathRuleEvaluator INSTANCE = new PathRuleEvaluator();

    private PathRuleEvaluator() {

    }

    @Override
    public boolean evaluate(IMatchRule rule, String path) {
        return evaluatePath(rule, path);
    }

    private boolean evaluatePath(IMatchRule rule, String path) {
        if (rule.getMatcher() == null || rule.getValue() == null || rule.getValue().isBlank()) {
            return false;
        }
        return StringMatchers.valueOf(rule.getMatcher().name())
                .getMatcher()
                .match(path, rule.getValue());
    }
}

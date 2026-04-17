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
        Operator operator =
                rule.getOperator() == null ? Operator.AND : rule.getOperator();
        return switch (operator) {
            case AND -> children.stream().allMatch(c -> recursiveEvaluator.evaluate(c, path));
            case OR -> children.stream().anyMatch(c -> recursiveEvaluator.evaluate(c, path));
            case NOT -> children.stream().noneMatch(c -> recursiveEvaluator.evaluate(c, path));
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
        boolean matched = StringMatchers.valueOf(rule.getMatcher().name())
                .getMatcher()
                .match(path, rule.getValue());
        Operator operator =
                rule.getOperator() == null ? Operator.AND : rule.getOperator();
        return (operator == Operator.NOT) != matched;
    }
}


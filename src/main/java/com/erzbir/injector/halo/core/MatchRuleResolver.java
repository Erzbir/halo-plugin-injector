package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.IMatchRule;
import com.erzbir.injector.api.MatchRuleType;
import com.erzbir.injector.api.Operator;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class MatchRuleResolver {
    public boolean matches(IMatchRule rule, String path) {
        if (rule == null || rule.getType() == null || path == null || path.isBlank()) {
            return false;
        }
        RuleEvaluator evaluator = MatchRuleEvaluators.resolve(rule.getType());
        if (evaluator == null) {
            return false;
        }
        boolean matched = evaluator.evaluate(rule, path);
        if (MatchRuleType.PATH.equals(rule.getType()) && Operator.NOT.equals(rule.getOperator())) {
            return !matched;
        }
        return matched;
    }
}

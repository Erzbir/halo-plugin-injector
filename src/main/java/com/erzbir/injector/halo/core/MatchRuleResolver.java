package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.IMatchRule;

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
        return evaluator != null && evaluator.evaluate(rule, path);
    }
}

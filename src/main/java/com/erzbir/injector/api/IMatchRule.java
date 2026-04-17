package com.erzbir.injector.api;

import java.util.List;

public interface IMatchRule {
    MatchRuleType getType();

    Operator getOperator();

    MatcherType getMatcher();

    String getValue();

    List<? extends IMatchRule> getChildren();
}

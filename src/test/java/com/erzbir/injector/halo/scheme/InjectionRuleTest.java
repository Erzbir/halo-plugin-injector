package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.api.MatcherType;
import com.erzbir.injector.api.Operator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectionRuleTest {

    @Test
    void shouldTreatNullEnabledAsDisabled() {
        InjectionRule rule = new InjectionRule();
        rule.setEnabled(null);

        assertFalse(rule.isEnabled());
    }

    @Test
    void shouldRequireNonBlankMatchWhenModeIsId() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.ID);
        rule.setMatchRule(MatchRule.pathRule(Operator.AND, MatcherType.PATH_PATTERN, "/**"));
        rule.setMatch(" ");

        assertFalse(rule.valid());

        rule.setMatch("content-root");

        assertTrue(rule.valid());
    }

    @Test
    void shouldRequireValidMatchRuleWhenModeIsId() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.ID);
        rule.setMatch("content-root");
        rule.setMatchRule(MatchRule.groupRule(Operator.AND));

        assertFalse(rule.valid());
    }

    @Test
    void shouldRequireNonBlankMatchWhenModeIsSelector() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.SELECTOR);
        rule.setMatchRule(MatchRule.pathRule(Operator.AND, MatcherType.PATH_PATTERN, "/**"));
        rule.setMatch("");

        assertFalse(rule.valid());

        rule.setMatch(".article-body");

        assertTrue(rule.valid());
    }

    @Test
    void shouldRequireValidMatchRuleWhenModeIsSelector() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.SELECTOR);
        rule.setMatch(".article-body");
        rule.setMatchRule(MatchRule.groupRule(Operator.AND));

        assertFalse(rule.valid());
    }

    @Test
    void shouldValidateByMatchRuleWhenModeIsHeadOrFooter() {
        InjectionRule rule = new InjectionRule();
        rule.setMode(InjectMode.HEAD);
        rule.setMatchRule(MatchRule.groupRule(Operator.AND));

        assertFalse(rule.valid());

        rule.setMatchRule(MatchRule.defaultRule());

        assertTrue(rule.valid());
    }
}

package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.scheme.InjectionRule;
import com.erzbir.halo.injector.scheme.MatchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InjectionRuleValidatorTest {
    private final InjectionRuleValidator validator = new InjectionRuleValidator();

    // why: 合法 regex 必须能顺利落库，避免后端校验误伤正常规则。
    @Test
    void shouldAllowValidRegexDuringWriteValidation() {
        InjectionRule rule = makeRule();
        rule.setMatchRule(makeGroup(MatchRule.pathRule(MatchRule.Matcher.REGEX, "^/posts/\\d+$")));

        assertDoesNotThrow(() -> validator.validateForWrite(rule).block());
    }

    // why: 非法 regex 要在写入期被明确拦下，不能等运行时才以“不生效”形式暴露问题。
    @Test
    void shouldRejectInvalidRegexDuringWriteValidation() {
        InjectionRule rule = makeRule();
        rule.setMatchRule(makeGroup(MatchRule.pathRule(MatchRule.Matcher.REGEX, "[")));

        InjectionRuleValidationException error = assertThrows(
                InjectionRuleValidationException.class,
                () -> validator.validateForWrite(rule).block()
        );

        assertEquals("matchRule.children[0].value：正则表达式无效，Unclosed character class", error.getReason());
    }

    // why: 模板 ID 规则只允许 EXACT/REGEX，写入期必须兜底拦截前后端约束不一致的数据。
    @Test
    void shouldRejectTemplateIdAntMatcherDuringWriteValidation() {
        InjectionRule rule = makeRule();
        MatchRule child = MatchRule.templateRule(MatchRule.Matcher.EXACT, "post");
        child.setMatcher(MatchRule.Matcher.ANT);
        rule.setMatchRule(makeGroup(child));

        InjectionRuleValidationException error = assertThrows(
                InjectionRuleValidationException.class,
                () -> validator.validateForWrite(rule).block()
        );

        assertEquals("matchRule.children[0].matcher：模板 ID 仅支持 REGEX 或 EXACT", error.getReason());
    }

    // why: 根节点固定为 GROUP，便于简单模式/JSON 模式共享统一的规则树结构。
    @Test
    void shouldRequireGroupAsRootNode() {
        InjectionRule rule = makeRule();
        rule.setMatchRule(MatchRule.pathRule(MatchRule.Matcher.ANT, "/**"));

        InjectionRuleValidationException error = assertThrows(
                InjectionRuleValidationException.class,
                () -> validator.validateForWrite(rule).block()
        );

        assertEquals("matchRule.type：根节点必须是 GROUP", error.getReason());
    }

    private InjectionRule makeRule() {
        InjectionRule rule = new InjectionRule();
        rule.setMatchRule(MatchRule.defaultRule());
        return rule;
    }

    private MatchRule makeGroup(MatchRule child) {
        MatchRule group = new MatchRule();
        group.setType(MatchRule.Type.GROUP);
        group.setChildren(java.util.List.of(child));
        return group;
    }
}

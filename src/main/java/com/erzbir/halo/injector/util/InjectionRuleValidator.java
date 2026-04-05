package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.scheme.InjectionRule;
import com.erzbir.halo.injector.scheme.MatchRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class InjectionRuleValidator {
    /**
     * 写入期兜底校验。
     * <p>
     * 前端负责给出更友好的编辑态提示；后端这里负责拦住会污染持久化数据的非法规则，
     * 尤其是非法正则，避免它们落库后等到运行时才以“规则不生效”的形式暴露出来。
     */
    public Mono<InjectionRule> validateForWrite(InjectionRule rule) {
        if (rule == null) {
            return Mono.error(new InjectionRuleValidationException("请求体不能为空"));
        }
        if ((InjectionRule.Mode.ID.equals(rule.getMode()) || InjectionRule.Mode.SELECTOR.equals(rule.getMode()))
                && !StringUtils.hasText(rule.getMatch())) {
            return Mono.error(new InjectionRuleValidationException("match：请填写匹配内容"));
        }
        try {
            MatchRule.validateForWrite(rule.getMatchRule());
            if ((InjectionRule.Mode.ID.equals(rule.getMode()) || InjectionRule.Mode.SELECTOR.equals(rule.getMode()))
                    && !MatchRule.supportsDomPathPrecheck(rule.getMatchRule())) {
                return Mono.error(new InjectionRuleValidationException(
                        "matchRule：ID/SELECTOR 模式下必须可按路径预筛；"
                                + "模板 ID 条件仅可作为已命中路径分支上的附加约束"
                ));
            }
            return Mono.just(rule);
        } catch (IllegalArgumentException e) {
            return Mono.error(new InjectionRuleValidationException(e.getMessage()));
        }
    }
}

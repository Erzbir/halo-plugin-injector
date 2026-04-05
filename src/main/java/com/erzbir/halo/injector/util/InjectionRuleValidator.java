package com.erzbir.halo.injector.util;

import com.erzbir.halo.injector.scheme.InjectionRule;
import com.erzbir.halo.injector.scheme.MatchRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
        return validateMatchRule(rule.getMatchRule(), "matchRule", true)
                .thenReturn(rule);
    }

    private Mono<Void> validateMatchRule(MatchRule rule, String path, boolean requireGroupRoot) {
        if (rule == null) {
            return Mono.error(new InjectionRuleValidationException(path + "：不能为空"));
        }
        if (rule.getType() == null) {
            return Mono.error(new InjectionRuleValidationException(path + ".type：不能为空"));
        }
        if (requireGroupRoot && rule.getType() != MatchRule.Type.GROUP) {
            return Mono.error(new InjectionRuleValidationException(path + ".type：根节点必须是 GROUP"));
        }
        return switch (rule.getType()) {
            case GROUP -> validateGroup(rule, path);
            case PATH -> validatePathRule(rule, path);
            case TEMPLATE_ID -> validateTemplateIdRule(rule, path);
        };
    }

    private Mono<Void> validateGroup(MatchRule rule, String path) {
        List<MatchRule> children = rule.getChildren();
        if (children == null || children.isEmpty()) {
            return Mono.error(new InjectionRuleValidationException(path + ".children：至少需要一个子条件"));
        }
        return Mono.when(
                java.util.stream.IntStream.range(0, children.size())
                        .mapToObj(index -> validateMatchRule(children.get(index), path + ".children[" + index + "]", false))
                        .toList()
        );
    }

    private Mono<Void> validatePathRule(MatchRule rule, String path) {
        if (!supportsPathMatcher(rule.getMatcher())) {
            return Mono.error(new InjectionRuleValidationException(
                    path + ".matcher：路径规则仅支持 ANT、REGEX、EXACT"
            ));
        }
        if (!StringUtils.hasText(rule.getValue())) {
            return Mono.error(new InjectionRuleValidationException(path + ".value：必须是非空字符串"));
        }
        if (rule.getMatcher() == MatchRule.Matcher.REGEX) {
            return validateRegex(rule.getValue(), path + ".value");
        }
        return Mono.empty();
    }

    private Mono<Void> validateTemplateIdRule(MatchRule rule, String path) {
        if (!supportsTemplateMatcher(rule.getMatcher())) {
            return Mono.error(new InjectionRuleValidationException(
                    path + ".matcher：模板 ID 仅支持 REGEX 或 EXACT"
            ));
        }
        if (!StringUtils.hasText(rule.getValue())) {
            return Mono.error(new InjectionRuleValidationException(path + ".value：必须是非空字符串"));
        }
        if (rule.getMatcher() == MatchRule.Matcher.REGEX) {
            return validateRegex(rule.getValue(), path + ".value");
        }
        return Mono.empty();
    }

    private Mono<Void> validateRegex(String value, String path) {
        try {
            // 写入期再校验一次，避免绕过前端后把非法正则持久化到库里。
            Pattern.compile(value);
            return Mono.empty();
        } catch (PatternSyntaxException e) {
            return Mono.error(new InjectionRuleValidationException(
                    path + "：正则表达式无效，" + e.getDescription()
            ));
        }
    }

    private boolean supportsPathMatcher(MatchRule.Matcher matcher) {
        return matcher == MatchRule.Matcher.ANT
                || matcher == MatchRule.Matcher.REGEX
                || matcher == MatchRule.Matcher.EXACT;
    }

    private boolean supportsTemplateMatcher(MatchRule.Matcher matcher) {
        return matcher == MatchRule.Matcher.REGEX
                || matcher == MatchRule.Matcher.EXACT;
    }
}

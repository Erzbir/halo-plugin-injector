package com.erzbir.halo.injector.scheme;

import com.erzbir.halo.injector.core.IInjectionRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "InjectionRule", group = "injector.erzbir.com", version = "v1alpha1",
        singular = "injectionRule", plural = "injectionRules")
public class InjectionRule extends AbstractExtension implements IInjectionRule {
    private String name = "";
    private String description = "";
    private Boolean enabled = true;
    private Mode mode = Mode.FOOTER;
    private String match = "";
    private MatchRule matchRule = MatchRule.defaultRule();
    private Position position = Position.APPEND;
    private Boolean wrapMarker = true;
    private Set<String> snippetIds = new LinkedHashSet<>();

    @Override
    public String getId() {
        return getMetadata().getName();
    }

    @Override
    public String getName() {
        if (this.name == null || this.name.isBlank()) {
            return getId();
        }
        return this.name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Boolean getWrapMarker() {
        return !Boolean.FALSE.equals(wrapMarker);
    }

    public boolean isValid() {
        boolean targetValid = !Mode.ID.equals(getMode()) && !Mode.SELECTOR.equals(getMode())
                || !getMatch().isBlank();
        boolean domModePathPrecheckValid = !Mode.ID.equals(getMode()) && !Mode.SELECTOR.equals(getMode())
                || MatchRule.supportsDomPathPrecheck(matchRule);
        return targetValid && domModePathPrecheckValid && matchRule != null && matchRule.isValid();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        validateDomModeMatchRule();
    }

    public void setMatchRule(MatchRule matchRule) {
        MatchRule.validateForWrite(matchRule);
        this.matchRule = matchRule;
        validateDomModeMatchRule();
    }

    /**
     * why: ID/SELECTOR 依赖 WebFilter 在真正读取模板上下文前做路径预筛；
     * 如果规则能在“不看路径、只看模板 ID”的分支上命中，就会迫使所有 HTML 响应都进入整页缓冲。
     */
    private void validateDomModeMatchRule() {
        if ((Mode.ID.equals(mode) || Mode.SELECTOR.equals(mode))
                && matchRule != null
                && !MatchRule.supportsDomPathPrecheck(matchRule)) {
            throw new IllegalArgumentException(
                    "matchRule：ID/SELECTOR 模式下必须可按路径预筛；"
                            + "模板 ID 条件仅可作为已命中路径分支上的附加约束"
            );
        }
    }
}

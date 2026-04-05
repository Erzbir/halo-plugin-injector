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
    private boolean wrapMarker = true;
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
    public boolean getWrapMarker() {
        return wrapMarker;
    }

    /**
     * why: 运行时这里只保留轻量级健康检查，
     * 真正昂贵或需要精确报错的位置放在写入期校验里，避免每次请求重复做同样工作。
     */
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

    /**
     * why: REMOVE 的语义会改变“是否还需要代码块”这一数据约束，
     * 因此在位置变更时立即收紧校验，避免对象进入短暂但可持久化的脏状态。
     */
    public void setPosition(Position position) {
        this.position = position;
        validateRemoveSnippetRelation();
    }

    public void setMatchRule(MatchRule matchRule) {
        MatchRule.validateForWrite(matchRule);
        this.matchRule = matchRule;
        validateDomModeMatchRule();
    }

    /**
     * why: 代码块关联是集合语义，复制一份可隔离外部可变集合；
     * 同时在赋值点就卡住 REMOVE + snippetIds 的非法组合，避免后续链路继续传播。
     */
    public void setSnippetIds(Set<String> snippetIds) {
        this.snippetIds = snippetIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(snippetIds);
        validateRemoveSnippetRelation();
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
                    "matchRule：元素 ID / CSS 选择器模式下，匹配规则里必须先有页面路径条件；"
                            + "模板 ID 只能在路径命中后继续缩小范围"
            );
        }
    }

    /**
     * why: REMOVE 的语义是“直接删掉元素节点”，不会消费任何代码内容；
     * 因此一旦仍允许关联代码块，就会制造误导性的脏数据和无意义的关联关系。
     */
    private void validateRemoveSnippetRelation() {
        if (Position.REMOVE.equals(position) && snippetIds != null && !snippetIds.isEmpty()) {
            throw new IllegalArgumentException("snippetIds：REMOVE 模式下无需关联代码块");
        }
    }
}

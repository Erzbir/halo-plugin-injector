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
        return targetValid && matchRule != null && matchRule.isValid();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * why: REMOVE 的语义会改变“是否还需要代码块”这一数据约束，
     * 也会让注释标记失去稳定落点；因此在位置变更时立即收紧相关约束，避免对象进入短暂但可持久化的脏状态。
     */
    public void setPosition(Position position) {
        this.position = position;
        validateRemoveSnippetRelation();
    }

    public void setMatchRule(MatchRule matchRule) {
        MatchRule.validateForWrite(matchRule);
        this.matchRule = matchRule;
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
     * why: REMOVE 会直接删掉目标节点，不会留下可包裹注释标记的稳定位置；
     * 因此一旦仍允许开启注释标记，就会制造“配置看似生效、实际无处输出”的误导性状态。
     */
    public void setWrapMarker(boolean wrapMarker) {
        this.wrapMarker = wrapMarker;
        validateRemoveWrapMarkerRelation();
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

    /**
     * why: 注释标记依赖注入内容或节点周围的稳定输出位置；
     * REMOVE 直接删除节点时不存在这样的落点，因此要拒绝这类无意义配置。
     */
    private void validateRemoveWrapMarkerRelation() {
        if (Position.REMOVE.equals(position) && wrapMarker) {
            throw new IllegalArgumentException("wrapMarker：REMOVE 模式下无需输出注释标记");
        }
    }
}

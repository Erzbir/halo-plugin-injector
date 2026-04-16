package com.erzbir.halo.injector.scheme;

import com.erzbir.halo.injector.core.IInjectionRule;
import com.erzbir.halo.injector.core.MatchRule;
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
    private Position position = Position.APPEND;
    private MatchRule matchRule = MatchRule.defaultRule();
    @Deprecated
    private Set<PathMatchRule> pathPatterns = new LinkedHashSet<>();
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
    public MatchRule getMatchRule() {
        if (matchRule != null) {
            return matchRule;
        }
        if (pathPatterns == null || pathPatterns.isEmpty()) {
            return MatchRule.defaultRule();
        }
        MatchRule root = new MatchRule();
        root.setType(MatchRule.Type.GROUP);
        root.setNegate(false);
        root.setOperator(MatchRule.Operator.OR);
        pathPatterns.stream()
                .map(PathMatchRule::getPathPattern)
                .filter(path -> path != null && !path.isBlank())
                .map(path -> MatchRule.pathRule(MatchRule.Matcher.ANT, path))
                .forEach(rule -> root.getChildren().add(rule));
        return root.getChildren().isEmpty() ? MatchRule.defaultRule() : root;
    }

    public boolean isValid() {
        if (Mode.ID.equals(getMode()) || Mode.SELECTOR.equals(getMode())) {
            return !getMatch().isBlank();
        }

        return getMatchRule().isValid();
    }

    @Data
    public static class PathMatchRule {
        private String pathPattern = "";
    }
}

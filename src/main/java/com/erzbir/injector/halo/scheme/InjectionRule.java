package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.api.InjectPosition;
import com.erzbir.injector.api.MatchRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "InjectionRule", group = "injector.erzbir.com", version = "v1alpha1",
        singular = "injectionRule", plural = "injectionRules")
public class InjectionRule extends AbstractExtension implements IInjectionRule {
    private String name = "";
    private String description = "";
    private Boolean enabled = false;
    private InjectMode mode = InjectMode.FOOTER;
    private String match = "";
    private InjectPosition position = InjectPosition.APPEND;
    private MatchRule matchRule = MatchRule.defaultRule();
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
        MatchRule root = MatchRule.groupRule(MatchRule.Operator.OR);
        return root.getChildren().isEmpty() ? MatchRule.defaultRule() : root;
    }

    public boolean isValid() {
        if (InjectMode.ID.equals(getMode()) || InjectMode.SELECTOR.equals(getMode())) {
            return !getMatch().isBlank();
        }

        return getMatchRule().isValid();
    }
}

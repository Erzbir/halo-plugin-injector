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
    private Position position = Position.APPEND;
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

    public boolean isValid() {
        if (Mode.ID.equals(getMode()) || Mode.SELECTOR.equals(getMode())) {
            return !getMatch().isBlank();
        }
        Set<InjectionRule.PathMatchRule> pathPatterns = getPathPatterns();
        return pathPatterns != null
                && !pathPatterns.isEmpty()
                && pathPatterns.stream()
                .anyMatch(p -> p.getPathPattern() != null && !p.getPathPattern().isBlank());
    }
}
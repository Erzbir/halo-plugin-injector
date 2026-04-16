package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.api.InjectPosition;
import com.erzbir.injector.api.MatchRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "InjectionRule mode must not be null")
    private InjectMode mode = InjectMode.HEAD;
    private String match = "";
    @NotNull(message = "InjectionRule position must not be null")
    private InjectPosition position = InjectPosition.APPEND;
    @Valid
    @NotNull(message = "InjectionRule matchRule must not be null")
    private MatchRule matchRule = MatchRule.defaultRule();
    @NotNull(message = "InjectionRule snippetIds must not be null")
    private Set<@NotBlank(message = "InjectionRule snippetId must not be blank") String> snippetIds = new LinkedHashSet<>();

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

    public boolean valid() {
        if (!getMatchRule().valid()) {
            return false;
        }

        if (InjectMode.ID.equals(getMode()) || InjectMode.SELECTOR.equals(getMode())) {
            return getMatch() != null && !getMatch().isBlank();
        }

        return true;
    }

    @AssertTrue(message = "InjectionRule is invalid for current mode or matchRule")
    @SuppressWarnings("unused")
    private boolean isInjectionRuleValid() {
        return valid();
    }
}

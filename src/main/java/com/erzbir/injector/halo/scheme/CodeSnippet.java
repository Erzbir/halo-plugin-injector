package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.ICodeSnippet;
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
@GVK(kind = "CodeSnippet", group = "injector.erzbir.com",
        version = "v1alpha1", singular = "codeSnippet", plural = "codeSnippets")
public class CodeSnippet extends AbstractExtension implements ICodeSnippet {
    private String name = "";
    private String code = "";
    private String description = "";
    private Boolean enabled = true;
    private Set<String> ruleIds = new LinkedHashSet<>();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getId() {
        return this.getMetadata().getName();
    }

    @Override
    public String getName() {
        if (name == null || name.isBlank()) {
            return getId();
        }
        return name;
    }

    public boolean isValid() {
        return code != null && !code.isBlank();
    }
}

package com.erzbir.injector.halo.scheme;

import com.erzbir.injector.api.ICodeSnippet;
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
@GVK(kind = "CodeSnippet", group = "injector.erzbir.com",
        version = "v1alpha1", singular = "codeSnippet", plural = "codeSnippets")
public class CodeSnippet extends AbstractExtension implements ICodeSnippet {
    private String name = "";
    @NotBlank(message = "CodeSnippet code must not be blank")
    private String code = "";
    private String description = "";
    private Boolean enabled = true;
    @NotNull(message = "CodeSnippet ruleIds must not be null")
    private Set<@NotBlank(message = "CodeSnippet ruleId must not be blank") String> ruleIds = new LinkedHashSet<>();

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
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

    public boolean valid() {
        return code != null && !code.isBlank();
    }

    @AssertTrue(message = "CodeSnippet code must not be blank")
    @SuppressWarnings("unused")
    private boolean isCodeSnippetValid() {
        return valid();
    }
}

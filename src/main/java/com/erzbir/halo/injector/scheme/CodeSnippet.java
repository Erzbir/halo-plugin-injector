package com.erzbir.halo.injector.scheme;

import com.erzbir.halo.injector.core.ICodeSnippet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "CodeSnippet", group = "injector.erzbir.com",
        version = "v1alpha1", singular = "codeSnippet", plural = "codeSnippets")
public class CodeSnippet extends AbstractExtension implements ICodeSnippet {
    @Schema(requiredMode = REQUIRED)
    private String name = "";

    @Schema(requiredMode = REQUIRED, minLength = 1)
    private String code = "";

    @Schema
    private String description = "";

    @Schema(defaultValue = "true")
    private Boolean enabled = true;

    @Schema
    private Set<String> ruleIds = new LinkedHashSet<>();

    private String generateName = "CodeSnippet-";

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

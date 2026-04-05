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
    private String name = "";
    private String code = "";
    private String description = "";
    private Boolean enabled = true;
    private Integer sortOrder;
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

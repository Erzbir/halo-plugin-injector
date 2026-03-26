package com.erzbir.halo.injector.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "CodeSnippet", group = "injector.halo.run",
        version = "v1alpha1", singular = "codeSnippet", plural = "codeSnippets")
public class CodeSnippet extends AbstractExtension {
    @Schema(requiredMode = REQUIRED)
    private String id;

    @Schema(requiredMode = REQUIRED)
    private String code;

    @Schema(requiredMode = REQUIRED)
    private Boolean enabled;

    public CodeSnippet(String code) {
        this.id = "Injector:" + UUID.randomUUID();
        this.code = code;
    }
}

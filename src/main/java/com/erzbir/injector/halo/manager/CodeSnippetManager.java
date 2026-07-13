package com.erzbir.injector.halo.manager;

import com.erzbir.injector.halo.scheme.CodeSnippet;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class CodeSnippetManager {

    private final ReactiveExtensionClient client;

    public CodeSnippetManager(ReactiveExtensionClient client) {
        this.client = client;
    }

    public Mono<CodeSnippet> get(String id) {
        return client.get(CodeSnippet.class, id);
    }
}

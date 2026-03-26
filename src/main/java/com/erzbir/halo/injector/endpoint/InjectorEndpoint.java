package com.erzbir.halo.injector.endpoint;

import com.erzbir.halo.injector.core.CodeSnippet;
import com.erzbir.halo.injector.core.CodeSnippetManager;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ApiVersion;

import java.util.List;

@ApiVersion("v1alpha1")
@RestController
@RequestMapping("/injector")
@AllArgsConstructor
public class InjectorEndpoint {

    private CodeSnippetManager codeSnippetManager;

    @PostMapping("/add")
    public Mono<Void> add(@RequestBody CodeSnippet snippet) {
        return Mono.fromRunnable(() -> codeSnippetManager.add(snippet));
    }

    @DeleteMapping("/remove/{id}")
    public Mono<Void> remove(@PathVariable String id) {
        return Mono.fromRunnable(() -> {
            try {
                codeSnippetManager.delete(id);
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CodeSnippet not found: " + id
                );
            }
        });
    }

    @PutMapping("/enable/{id}")
    public Mono<Void> enable(@PathVariable String id) {
        return Mono.fromRunnable(() -> {
            codeSnippetManager.enable(id);
        });
    }

    @PutMapping("/disable/{id}")
    public Mono<Void> disable(@PathVariable String id) {
        return Mono.fromRunnable(() -> {
            codeSnippetManager.disable(id);
        });
    }

    @GetMapping("/list")
    public Mono<List<CodeSnippet>> list() {
        return Mono.fromCallable(codeSnippetManager::list);
    }

    @GetMapping("/{id}")
    public Mono<CodeSnippet> get(@PathVariable String id) {
        return Mono.fromCallable(() ->
                codeSnippetManager.list().stream()
                        .filter(s -> s.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "CodeSnippet not found: " + id
                        ))
        );
    }
}
package com.erzbir.halo.injector.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.SettingFetcher;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CodeSnippetManager implements Closeable, AutoCloseable {

    protected final SettingFetcher settingFetcher;
    protected final ReactiveExtensionClient client;

    private final Map<String, CodeSnippet> cacheMap = new ConcurrentHashMap<>();

    public CodeSnippetManager(SettingFetcher settingFetcher, ReactiveExtensionClient client) {
        this.settingFetcher = settingFetcher;
        this.client = client;
        refresh();
    }

    private void refresh() {
        clearCache();
        client.list(CodeSnippet.class, null, null)
                .doOnNext(snippet -> cacheMap.put(snippet.getId(), snippet))
                .doOnError(e -> log.error("Failed to refresh CodeSnippet cache", e))
                .blockLast();
    }

    public void add(CodeSnippet snippet) {
        Optional<CodeSnippet> existing =
                client.fetch(CodeSnippet.class, snippet.getId()).blockOptional();

        if (existing.isPresent()) {
            snippet.getMetadata().setVersion(existing.get().getMetadata().getVersion());
            client.update(snippet).block();
            log.debug("Updated CodeSnippet: {}", snippet.getId());
        } else {
            client.create(snippet).block();
            log.debug("Created CodeSnippet: {}", snippet.getId());
        }
        cacheMap.put(snippet.getId(), snippet);
    }

    public void delete(String id) {
        CodeSnippet removed = cacheMap.remove(id);
        if (removed == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "CodeSnippet not found: " + id
            );
        }
        client.delete(removed)
                .doOnError(e -> {
                    cacheMap.put(id, removed);
                    log.error("Failed to delete CodeSnippet: {}", id, e);
                })
                .block();
        log.debug("Deleted CodeSnippet: {}", id);
    }

    public void enable(String id) {
        CodeSnippet snippet = cacheMap.get(id);
        if (snippet == null) {
            log.warn("Enable called on unknown CodeSnippet: {}", id);
            return;
        }
        snippet.setEnabled(true);
        client.update(snippet).block();
        log.debug("Enabled CodeSnippet: {}", id);
    }

    public void disable(String id) {
        CodeSnippet snippet = cacheMap.get(id);
        if (snippet == null) {
            log.warn("Disable called on unknown CodeSnippet: {}", id);
            return;
        }
        snippet.setEnabled(false);
        client.update(snippet).block();
        log.debug("Disabled CodeSnippet: {}", id);
    }

    public void clearCache() {
        cacheMap.clear();
    }

    public List<CodeSnippet> list() {
        return cacheMap.values().stream().toList();
    }

    @Override
    public void close() {
        clearCache();
    }
}
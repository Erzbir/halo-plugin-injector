package com.erzbir.injector.halo.manager;

import com.erzbir.injector.halo.scheme.CodeSnippet;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeSnippetManagerTest {

    @Test
    void shouldDelegateGetToReactiveClient() {
        ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
        CodeSnippetManager manager = new CodeSnippetManager(client);
        CodeSnippet snippet = new CodeSnippet();
        when(client.get(CodeSnippet.class, "s1")).thenReturn(Mono.just(snippet));

        CodeSnippet result = manager.get("s1").block();

        assertSame(snippet, result);
    }

    @Test
    void shouldPropagateErrorFromReactiveClient() {
        ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
        CodeSnippetManager manager = new CodeSnippetManager(client);
        when(client.get(CodeSnippet.class, "s1")).thenReturn(Mono.error(new IllegalStateException("boom")));

        assertThrows(RuntimeException.class, () -> manager.get("s1").block());
    }
}

package com.erzbir.halo.injector.setting;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class BasicSettingSupplier implements Supplier<Mono<BasicConfig>> {
    private final ReactiveSettingFetcher fetcher;

    @Override
    public Mono<BasicConfig> get() {
        return fetcher.fetch("basic", BasicConfig.class);
    }
}

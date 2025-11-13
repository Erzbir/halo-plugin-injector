package com.erzbir.halo.injector.core;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface HTMLInjector extends Injector {
    String inject(String html, InjectionRule rule);
}

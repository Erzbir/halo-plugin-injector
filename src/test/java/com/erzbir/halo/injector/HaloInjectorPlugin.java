package com.erzbir.halo.injector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Slf4j
@Component
public class HaloInjectorPlugin extends BasePlugin {
    private final ApplicationContext applicationContext;

    public HaloInjectorPlugin(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }




    @Override
    public void stop() {
    }
}

package com.erzbir.halo.injector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentLruCache;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import run.halo.app.plugin.BasePlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

@Slf4j
@Component
public class TestPlugin extends BasePlugin {

    private final ApplicationContext applicationContext;

    public TestPlugin(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() {
        try {
            Object extensionGetter = getExtensionGetter();
            BeanFactory beanFactory = getBeanFactory(extensionGetter);

            Object templateEngineManager = beanFactory.getBean("templateEngineManager");
            ConcurrentLruCache<?, ?> engineCache = getEngineCache(templateEngineManager);

            injectGenerator(engineCache, templateEngineManager);

            log.info("TestPlugin: template engine dialect injection completed.");
        } catch (Exception e) {
            log.error("TestPlugin: failed to inject template engine dialect.", e);
            throw new IllegalStateException("Dialect injection failure", e);
        }
    }

    @Override
    public void stop() {
        // TODO: restore original generator if needed
        log.info("TestPlugin stopped. (restore logic not implemented)");
    }

    private Object getExtensionGetter() {
        ApplicationContext root = applicationContext.getParent();
        if (root == null) {
            throw new IllegalStateException("Root ApplicationContext is null.");
        }

        return root.getBean("extensionGetter");
    }

    private BeanFactory getBeanFactory(Object extensionGetter)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = extensionGetter.getClass().getDeclaredField("beanFactory");
        field.setAccessible(true);

        Object result = field.get(extensionGetter);
        if (!(result instanceof BeanFactory)) {
            throw new IllegalStateException("beanFactory field is not a BeanFactory");
        }

        return (BeanFactory) result;
    }

    private ConcurrentLruCache<?, ?> getEngineCache(Object templateEngineManager)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = templateEngineManager.getClass().getDeclaredField("engineCache");
        field.setAccessible(true);

        Object cache = field.get(templateEngineManager);
        if (!(cache instanceof ConcurrentLruCache<?, ?>)) {
            throw new IllegalStateException("engineCache is not a ConcurrentLruCache");
        }

        return (ConcurrentLruCache<?, ?>) cache;
    }

    @SuppressWarnings({"rawtypes"})
    private void injectGenerator(
            ConcurrentLruCache engineCache,
            Object templateEngineManager
    ) throws NoSuchFieldException, IllegalAccessException {

        Field generatorField = engineCache.getClass().getDeclaredField("generator");
        generatorField.setAccessible(true);

        Function<Object, ISpringWebFluxTemplateEngine> newGenerator = key -> {
            TemplateEngine templateEngine = generateEngine(templateEngineManager, key);
            if (templateEngine == null) {
                return null;
            }

            try {
                templateEngine.addDialect(new TestProcessorDialect());
            } catch (Exception ex) {
                log.error("Failed to add TestDialect.", ex);
            }

            return (ISpringWebFluxTemplateEngine) templateEngine;
        };

        generatorField.set(engineCache, newGenerator);
    }

    private TemplateEngine generateEngine(Object templateEngineManager, Object key) {
        try {
            Method method = templateEngineManager.getClass()
                    .getDeclaredMethod("templateEngineGenerator", key.getClass());
            method.setAccessible(true);
            return (TemplateEngine) method.invoke(templateEngineManager, key);
        } catch (Exception e) {
            log.error("Failed to generate TemplateEngine.", e);
            return null;
        }
    }
}

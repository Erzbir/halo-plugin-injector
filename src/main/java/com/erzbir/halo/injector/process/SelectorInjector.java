package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.TemplateFooterProcessor;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class SelectorInjector extends AbstractInjector implements TemplateFooterProcessor {
    public SelectorInjector(ReactiveSettingFetcher reactiveSettingFetcher) {
        super(reactiveSettingFetcher);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler, IModel model) {
        return inject(context, model, InjectionRule.Location.SELECTOR);
    }

    protected String processRuleCode(InjectionRule rule) {
        String code = rule.getCode();
        String selector = rule.getSelector();
        if (StringUtils.hasText(selector)) {
            code = ScriptTemplates.SELECTOR_INJECTION_SCRIPT.formatted(code, selector, selector);
        }
        return code;
    }
}

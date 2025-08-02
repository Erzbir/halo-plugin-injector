package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import com.erzbir.halo.injector.util.ContextUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class ElementIDInjector extends AbstractTemplateInjector implements TemplateHeadProcessor {

    public ElementIDInjector(InjectService injectService) {
        super(injectService);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return inject(new HeadInjectorProvider(context, model, structureHandler),
            ContextUtil.getPath(context),
            InjectionRule.Location.ID);
    }

    public String processRuleCode(InjectionRule rule) {
        String code = rule.getCode();
        String elementId = rule.getId();
        if (StringUtils.hasText(elementId)) {
            code = ScriptTemplates.ELEMENT_INJECTION_SCRIPT.formatted(code, elementId, elementId);
        }
        return code;
    }
}
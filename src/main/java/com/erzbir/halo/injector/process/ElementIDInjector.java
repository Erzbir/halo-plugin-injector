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
public class ElementIDInjector extends AbstrictInjector implements TemplateFooterProcessor {

    public ElementIDInjector(ReactiveSettingFetcher reactiveSettingFetcher) {
        super(reactiveSettingFetcher);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IProcessableElementTag tag,
        IElementTagStructureHandler structureHandler, IModel model) {
        return inject(context, model, InjectionRule.Location.ID);
    }

    protected String processRuleCode(InjectionRule rule) {
        String code = rule.getCode();
        String elementId = rule.getId();
        if (StringUtils.hasText(elementId)) {
            code = """
                    <script defer>
                      let code = '%s';
                      let dom = new DOMParser().parseFromString(code, 'text/html');
                      let element = dom.body.firstElementChild;
                      document.getElementById('%s').appendChild(element);
                    </script>
                """.formatted(code, elementId);
        }
        String comment_start = "<!-- PluginInjector start -->";
        String comment_end = "<!-- PluginInjector end -->";
        return comment_start + code + comment_end;
    }

}
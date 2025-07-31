package com.erzbir.halo.injector.process;

import com.erzbir.halo.injector.setting.InjectionRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class SelectorInjector extends AbstrictInjector implements TemplateHeadProcessor {
    public SelectorInjector(ReactiveSettingFetcher reactiveSettingFetcher) {
        super(reactiveSettingFetcher);
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        return inject(context, model, InjectionRule.Location.SELECTOR);
    }

    protected String processRuleCode(InjectionRule rule) {
        String code = rule.getCode();
        String selector = rule.getSelector();
        if (StringUtils.hasText(selector)) {
            code = """
                    <script defer>
                      let html = '%s';
                      let dom = new DOMParser().parseFromString(html, 'text/html');
                      let element = dom.body.firstElementChild;
                      document.querySelector('%s').appendChild(element);
                    </script>
                """.formatted(code, selector);
        }
        String comment_start = "<!-- PluginInjector start -->";
        String comment_end = "<!-- PluginInjector end -->";
        return comment_start + code + comment_end;
    }
}

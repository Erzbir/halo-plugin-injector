package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.halo.util.HTMLInjectUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class SelectorInjector implements HTMLInjector {

    @Override
    public String inject(Document html, Code code, IInjectionRule rule, Void context) {
        Elements elements = html.select(rule.getMatch());
        if (elements.isEmpty()) {
            return html.html();
        }

        for (Element element : elements) {
            HTMLInjectUtil.inject(element, processCode(code.raw()), rule.getPosition());
        }

        html.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return html.html();
    }
}

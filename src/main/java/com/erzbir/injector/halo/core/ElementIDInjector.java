package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.halo.util.HTMLInjectUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public class ElementIDInjector implements HTMLInjector {
    @Override
    public String inject(Document html, Code code, IInjectionRule rule, Void context) {
        Element element = html.getElementById(rule.getMatch());
        if (element == null) {
            return html.html();
        }

        HTMLInjectUtil.inject(element, processCode(code.raw()), rule.getPosition());

        html.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return html.html();
    }
}
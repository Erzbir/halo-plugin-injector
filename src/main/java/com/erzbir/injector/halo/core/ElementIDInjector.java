package com.erzbir.injector.halo.core;

import com.erzbir.injector.api.Code;
import com.erzbir.injector.api.IInjectionRule;
import com.erzbir.injector.halo.util.HTMLInjectUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Component
public class ElementIDInjector implements HTMLInjector {
    @Override
    public String inject(String html, Code code, IInjectionRule rule, Void context) {
        Document doc = Jsoup.parse(html);

        Element element = doc.getElementById(rule.getMatch());
        if (element == null) {
            return html;
        }

        HTMLInjectUtil.inject(element, processCode(code.raw()), rule.getPosition());

        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return doc.html();
    }
}
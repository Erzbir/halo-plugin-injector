package com.erzbir.halo.injector.core;

import com.erzbir.halo.injector.util.InjectUtil;
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
    public String inject(String html, InjectionRule rule) {
        Document doc = Jsoup.parse(html);

        Element element = doc.getElementById(rule.getId());
        if (element == null) {
            return html;
        }

        InjectUtil.inject(element, processCode(rule.getCode()), rule.getPosition());

        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return doc.html();
    }
}
package com.erzbir.halo.injector.core;

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
    public String inject(String html, String match, String code, IInjectionRule.Position position, boolean wrapMarker) {
        Document doc = Jsoup.parse(html);

        Element element = doc.getElementById(match);
        if (element == null) {
            return html;
        }

        InjectUtil.inject(element, processCode(code, wrapMarker), position);

        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return doc.html();
    }
}

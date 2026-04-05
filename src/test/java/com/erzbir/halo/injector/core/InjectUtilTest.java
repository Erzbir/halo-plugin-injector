package com.erzbir.halo.injector.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InjectUtilTest {
    @Test
    void shouldRemoveElementWhenPositionIsRemove() {
        Document document = Jsoup.parse("<div><span id='target'>hello</span><p>world</p></div>");
        Element target = document.getElementById("target");

        InjectUtil.inject(target, "<em>ignored</em>", IInjectionRule.Position.REMOVE);

        assertNull(document.getElementById("target"));
        assertEquals("<div><p>world</p></div>", document.body().html());
    }
}

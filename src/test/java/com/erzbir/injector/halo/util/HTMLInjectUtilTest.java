package com.erzbir.injector.halo.util;

import com.erzbir.injector.api.InjectPosition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HTMLInjectUtilTest {

    @Test
    void shouldAppendCode() {
        Document document = Jsoup.parse("<div id='x'><span>base</span></div>");
        Element element = document.getElementById("x");

        HTMLInjectUtil.inject(element, "<em>tail</em>", InjectPosition.APPEND);

        assertEquals("<span>base</span><em>tail</em>", element.html());
    }

    @Test
    void shouldPrependCode() {
        Document document = Jsoup.parse("<div id='x'><span>base</span></div>");
        Element element = document.getElementById("x");

        HTMLInjectUtil.inject(element, "<em>head</em>", InjectPosition.PREPEND);

        assertEquals("<em>head</em><span>base</span>", element.html());
    }

    @Test
    void shouldInsertBeforeElement() {
        Document document = Jsoup.parse("<body><div id='x'>base</div></body>");
        Element element = document.getElementById("x");

        HTMLInjectUtil.inject(element, "<em>before</em>", InjectPosition.BEFORE);

        String body = document.body().html();
        assertTrue(body.contains("<em>before</em>"));
        assertTrue(body.contains("<div id=\"x\">base</div>"));
        assertTrue(body.indexOf("<em>before</em>") < body.indexOf("<div id=\"x\">base</div>"));
    }

    @Test
    void shouldInsertAfterElement() {
        Document document = Jsoup.parse("<body><div id='x'>base</div></body>");
        Element element = document.getElementById("x");

        HTMLInjectUtil.inject(element, "<em>after</em>", InjectPosition.AFTER);

        String body = document.body().html();
        assertTrue(body.contains("<em>after</em>"));
        assertTrue(body.contains("<div id=\"x\">base</div>"));
        assertTrue(body.indexOf("<div id=\"x\">base</div>") < body.indexOf("<em>after</em>"));
    }

    @Test
    void shouldReplaceElement() {
        Document document = Jsoup.parse("<body><div id='x'>base</div></body>");
        Element element = document.getElementById("x");

        HTMLInjectUtil.inject(element, "<section id='x2'>next</section>", InjectPosition.REPLACE);

        assertEquals("<section id=\"x2\">next</section>", document.body().html());
    }
}

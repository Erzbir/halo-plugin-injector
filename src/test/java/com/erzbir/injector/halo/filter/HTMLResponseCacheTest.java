package com.erzbir.injector.halo.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HTMLResponseCacheTest {

    @Test
    void shouldReturnCachedValueWhenPathAndHtmlAreSame() {
        HTMLResponseCache cache = new HTMLResponseCache();

        assertNull(cache.get("/posts/1", "<html>a</html>"));
        cache.put("/posts/1", "<html>a</html>", "processed-1");
        String second = cache.get("/posts/1", "<html>a</html>");

        assertEquals("processed-1", second);
    }

    @Test
    void shouldRecomputeWhenHtmlChanges() {
        HTMLResponseCache cache = new HTMLResponseCache();

        cache.put("/posts/1", "<html>a</html>", "processed-1");
        String second = cache.get("/posts/1", "<html>b</html>");

        assertNull(second);
    }
}

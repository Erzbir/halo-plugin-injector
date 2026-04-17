package com.erzbir.injector.halo.filter;

import com.erzbir.injector.halo.util.FingerprintUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HTMLResponseCacheTest {

    @Test
    void shouldReturnCachedValueWhenPathAndHtmlAreSame() {
        long fingerprint = FingerprintUtil.fnv1a64("<html>a</html>");
        assertNull(HTMLResponseCache.get("/posts/1", fingerprint));
        HTMLResponseCache.put("/posts/1", fingerprint, "processed-1");
        String second = HTMLResponseCache.get("/posts/1", fingerprint);

        assertEquals("processed-1", second);
    }

    @Test
    void shouldRecomputeWhenHtmlChanges() {
        HTMLResponseCache.put("/posts/1", FingerprintUtil.fnv1a64("<html>a</html>"), "processed-1");
        String second = HTMLResponseCache.get("/posts/1", FingerprintUtil.fnv1a64("<html>b</html>"));

        assertNull(second);
    }
}

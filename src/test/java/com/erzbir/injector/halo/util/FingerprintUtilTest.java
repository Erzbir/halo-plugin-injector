package com.erzbir.injector.halo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FingerprintUtilTest {

    @Test
    void shouldGenerateSameFNVFingerprintForSameInput() {
        String html = "<html><body>same</body></html>";
        assertEquals(FingerprintUtil.fnv1a64(html), FingerprintUtil.fnv1a64(html));
    }

    @Test
    void shouldGenerateDifferentFNVFingerprintForDifferentInput() {
        assertNotEquals(
                FingerprintUtil.fnv1a64("<html><body>a</body></html>"),
                FingerprintUtil.fnv1a64("<html><body>b</body></html>")
        );
    }

    @Test
    void shouldGenerateSameCRC32CFingerprintForSameInput() {
        String html = "<html><body>same</body></html>";
        assertEquals(FingerprintUtil.crc32c64(html), FingerprintUtil.crc32c64(html));
    }
}

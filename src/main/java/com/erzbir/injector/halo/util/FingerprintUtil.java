package com.erzbir.injector.halo.util;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32C;

public final class FingerprintUtil {

    private static final long FNV_64_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_64_PRIME = 0x100000001b3L;

    private FingerprintUtil() {
    }

    public static long fnv1a64(String input) {
        long hash = FNV_64_OFFSET_BASIS;
        for (int i = 0; i < input.length(); i++) {
            hash ^= input.charAt(i);
            hash *= FNV_64_PRIME;
        }
        return hash ^ input.length();
    }

    public static long crc32c64(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        CRC32C crc32c = new CRC32C();
        crc32c.update(bytes, 0, bytes.length);
        long crc = crc32c.getValue();
        return (crc << 32) | (bytes.length & 0xFFFFFFFFL);
    }
}

package com.erzbir.injector.halo.filter;

import com.erzbir.injector.halo.util.FingerprintUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

class HTMLResponseCache {

    private static final long RESPONSE_CACHE_TTL_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final int RESPONSE_CACHE_MAX_SIZE = 1024;

    private final ConcurrentHashMap<ResponseCacheKey, CachedResponse> responseCache = new ConcurrentHashMap<>();

    public String get(String path, String html) {
        return get(path, FingerprintUtil.fnv1a64(html));
    }

    public String get(String path, long htmlFingerprint) {
        ResponseCacheKey cacheKey = new ResponseCacheKey(path, htmlFingerprint);
        long now = System.currentTimeMillis();
        CachedResponse cached = responseCache.get(cacheKey);
        if (cached != null && cached.expiresAtMillis() > now) {
            return cached.html();
        }
        return null;
    }

    public void put(String path, String html, String processedHtml) {
        put(path, FingerprintUtil.fnv1a64(html), processedHtml);
    }

    public void put(String path, long htmlFingerprint, String processedHtml) {
        ResponseCacheKey cacheKey = new ResponseCacheKey(path, htmlFingerprint);
        if (responseCache.size() >= RESPONSE_CACHE_MAX_SIZE) {
            responseCache.clear();
        }
        long expiresAtMillis = System.currentTimeMillis() + RESPONSE_CACHE_TTL_MILLIS;
        responseCache.put(cacheKey, new CachedResponse(expiresAtMillis, processedHtml));
    }

    private record ResponseCacheKey(String path, long htmlFingerprint) {
    }

    private record CachedResponse(long expiresAtMillis, String html) {
    }
}

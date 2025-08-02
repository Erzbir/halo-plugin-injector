package com.erzbir.halo.injector.setting;

import java.util.List;
import lombok.Data;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Data
public class InjectionRule {
    private String code = "";
    private List<PathMatchRule> pathPatterns;
    private String location = "footer";
    private String id = "";
    private String selector = "";

    public Location getLocationEnum() {
        try {
            return Location.valueOf(location.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Location.FOOTER;
        }
    }

    public boolean isValid() {
        return code != null && !code.trim().isEmpty()
            && pathPatterns != null && !pathPatterns.isEmpty()
            && pathPatterns.stream()
            .anyMatch(
                pattern -> pattern.pathPattern != null && !pattern.pathPattern.trim().isEmpty());
    }

    public enum Location {
        HEAD,
        FOOTER,
        ID,
        SELECTOR;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @Data
    public static class PathMatchRule {
        private String pathPattern = "";
    }
}

package com.erzbir.halo.injector.core;

import java.util.List;
import lombok.Data;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Data
public class InjectionRule {
    private Boolean enabled = true;
    private String code = "";
    private List<PathMatchRule> pathPatterns;
    private String mode = "footer";
    private String id = "";
    private String selector = "";
    private String position = "inner";


    public Position getPosition() {
        try {
            return Position.valueOf(position.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Position.APPEND;
        }
    }

    public Mode getMode() {
        try {
            return Mode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mode.FOOTER;
        }
    }

    public boolean isValid() {
        return code != null && !code.trim().isEmpty()
            && pathPatterns != null && !pathPatterns.isEmpty()
            && pathPatterns.stream()
            .anyMatch(
                pattern -> pattern.pathPattern != null && !pattern.pathPattern.trim().isEmpty());
    }

    public enum Mode {
        HEAD,
        FOOTER,
        ID,
        SELECTOR;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum Position {
        APPEND,
        PREPEND,
        BEFORE,
        AFTER;

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

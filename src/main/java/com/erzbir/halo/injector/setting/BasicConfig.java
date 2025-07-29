package com.erzbir.halo.injector.setting;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Erzbir
 * @since 1.0.0
 */
@Data
public class BasicConfig {
    private List<InjectionRule> rules = new ArrayList<>();

    public List<InjectionRule> nullSafeRules() {
        return ObjectUtils.defaultIfNull(rules, List.of());
    }

    public List<InjectionRule> getRulesByLocation(InjectionRule.Location location) {
        return nullSafeRules().stream()
            .filter(rule -> location.equals(rule.getLocationEnum()))
            .filter(InjectionRule::isValid)
            .toList();
    }
}
package com.erzbir.halo.injector.setting;

import com.erzbir.halo.injector.core.InjectionRule;
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
}
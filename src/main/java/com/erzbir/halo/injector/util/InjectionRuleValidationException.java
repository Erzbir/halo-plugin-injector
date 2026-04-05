package com.erzbir.halo.injector.util;

import org.springframework.web.server.ServerWebInputException;

public class InjectionRuleValidationException extends ServerWebInputException {
    public InjectionRuleValidationException(String reason) {
        super(reason);
    }
}

package com.erzbir.injector.halo.util;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.ITemplateContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ContextUtilTest {

    @Test
    void shouldReturnEmptyWhenContextIsNotWebContext() {
        ITemplateContext context = mock(ITemplateContext.class);

        String path = ContextUtil.getPath(context);

        assertEquals("", path);
    }
}

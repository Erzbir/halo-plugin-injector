package com.erzbir.halo.injector;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

public class TestProcessorDialect extends AbstractProcessorDialect {
    private static final String DIALECT_NAME = "testProcessorDialect";

    public TestProcessorDialect() {
        super(DIALECT_NAME, "", StandardDialect.PROCESSOR_PRECEDENCE * 10);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> set = new HashSet<>();
        set.add(new TestHTMLProcessor(dialectPrefix));
        return set;
    }

    static class TestHTMLProcessor extends AbstractElementModelProcessor {
        private static final String TAG_NAME = "html";
        private static final int PRECEDENCE = 1000;

        public TestHTMLProcessor(String dialectPrefix) {
            super(TemplateMode.HTML, dialectPrefix, TAG_NAME, false, null, false, PRECEDENCE);
        }

        @Override
        protected void doProcess(ITemplateContext context, IModel model,
            IElementModelStructureHandler structureHandler) {
        }
    }
}

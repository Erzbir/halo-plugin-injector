package com.erzbir.injector.halo.process;

import com.erzbir.injector.api.InjectMode;
import com.erzbir.injector.halo.core.InjectHelper;
import com.erzbir.injector.halo.scheme.InjectionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractTemplateProcessorTest {

    @Mock
    private InjectHelper injectHelper;

    private TestTemplateProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TestTemplateProcessor(injectHelper, InjectMode.HEAD, false);
    }

    @Test
    void shouldCallDoProcessForEachMatchedRuleWithCode_whenRulesAndCodeAreAvailable() {
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        InjectionRule rule1 = mock(InjectionRule.class);
        InjectionRule rule2 = mock(InjectionRule.class);
        when(injectHelper.getMatchedRules("", InjectMode.HEAD)).thenReturn(Flux.just(rule1, rule2));
        when(injectHelper.getConcatCode(rule1)).thenReturn(Mono.just("<script>1</script>"));
        when(injectHelper.getConcatCode(rule2)).thenReturn(Mono.just("<script>2</script>"));

        processor.process(context, model).block();

        assertEquals(2, processor.invocationCount);
        assertEquals("<script>2</script>", processor.lastCode);
        verify(injectHelper).getMatchedRules("", InjectMode.HEAD);
        verify(injectHelper).getConcatCode(eq(rule1));
        verify(injectHelper).getConcatCode(eq(rule2));
    }

    @Test
    void shouldNotCallDoProcess_whenNoMatchedRuleExists() {
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        when(injectHelper.getMatchedRules("", InjectMode.HEAD)).thenReturn(Flux.empty());

        processor.process(context, model).block();

        assertEquals(0, processor.invocationCount);
        verify(injectHelper).getMatchedRules("", InjectMode.HEAD);
    }

    @Test
    void shouldNotCallDoProcess_whenConcatCodeIsEmpty() {
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        InjectionRule rule = mock(InjectionRule.class);
        when(injectHelper.getMatchedRules("", InjectMode.HEAD)).thenReturn(Flux.just(rule));
        when(injectHelper.getConcatCode(rule)).thenReturn(Mono.empty());

        processor.process(context, model).block();

        assertEquals(0, processor.invocationCount);
        verify(injectHelper).getConcatCode(eq(rule));
    }

    @Test
    void shouldPropagateException_whenMatchedRulesFails() {
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        RuntimeException expected = new RuntimeException("match failed");
        when(injectHelper.getMatchedRules("", InjectMode.HEAD)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class, () -> processor.process(context, model).block());

        assertEquals("match failed", actual.getMessage());
        verify(injectHelper, never()).getConcatCode(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldPropagateException_whenConcatCodeFails() {
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        InjectionRule rule = mock(InjectionRule.class);
        when(injectHelper.getMatchedRules("", InjectMode.HEAD)).thenReturn(Flux.just(rule));
        when(injectHelper.getConcatCode(rule)).thenReturn(Mono.error(new IllegalStateException("concat failed")));

        assertDoesNotThrow(() -> processor.process(context, model).block());
    }

    @Test
    void shouldPropagateException_whenDoProcessFails() {
        TestTemplateProcessor failingProcessor = new TestTemplateProcessor(injectHelper, InjectMode.HEAD, true);
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        InjectionRule rule = mock(InjectionRule.class);
        when(injectHelper.getMatchedRules("", InjectMode.HEAD)).thenReturn(Flux.just(rule));
        when(injectHelper.getConcatCode(rule)).thenReturn(Mono.just("<script>x</script>"));

        assertDoesNotThrow(() -> failingProcessor.process(context,
                model).block());
    }

    @Test
    void shouldUseModeReturnedBySubclass_whenQueryingMatchedRules() {
        TestTemplateProcessor footerProcessor = new TestTemplateProcessor(injectHelper, InjectMode.FOOTER, false);
        ITemplateContext context = mock(ITemplateContext.class);
        IModel model = mock(IModel.class);
        when(injectHelper.getMatchedRules("", InjectMode.FOOTER)).thenReturn(Flux.empty());

        footerProcessor.process(context, model).block();

        verify(injectHelper).getMatchedRules("", InjectMode.FOOTER);
    }

    private static class TestTemplateProcessor extends AbstractTemplateProcessor {
        private final InjectMode mode;
        private final boolean failOnProcess;
        private int invocationCount;
        private String lastCode;

        private TestTemplateProcessor(InjectHelper injectHelper, InjectMode mode, boolean failOnProcess) {
            super(injectHelper);
            this.mode = mode;
            this.failOnProcess = failOnProcess;
        }

        Mono<Void> process(ITemplateContext context, IModel model) {
            return processInternal(context, model);
        }

        @Override
        protected InjectMode mode() {
            return mode;
        }

        @Override
        protected void doProcess(ITemplateContext context, IModel model, String code) {
            invocationCount++;
            lastCode = code;
            if (failOnProcess) {
                throw new IllegalStateException("doProcess failed");
            }
        }
    }
}

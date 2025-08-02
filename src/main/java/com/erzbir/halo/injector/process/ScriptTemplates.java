package com.erzbir.halo.injector.process;

/**
 * @author Erzbir
 * @since 1.0.0
 */
public interface ScriptTemplates {
    String SELECTOR_INJECTION_SCRIPT = """
              <script>
                    document.addEventListener('DOMContentLoaded', () => {
                        let code = '%s';
                        let dom = new DOMParser().parseFromString(code, 'text/html');
                        let injection = dom.body.firstElementChild;
                        let element = document.querySelector('%s');
                        if (!element) return;
                        let key = "%s";
                        const existing = element.querySelector(`[data-injected-selector='${key}']`);
                        if (existing) existing.remove();
                        injection.setAttribute('data-injected-selector', key);
                        element.appendChild(injection);
                    });
                </script>
        """;
    String ELEMENT_INJECTION_SCRIPT = """
                   <script>
                    document.addEventListener('DOMContentLoaded', () => {
                        let code = '%s';
                        let dom = new DOMParser().parseFromString(code, 'text/html');
                        let injection = dom.body.firstElementChild;
                        let element = document.getElementById('%s');
                        if (!element) return;
                        let key = "%s";
                        const existing = element.querySelector(`[data-injected-id='${key}']`);
                        if (existing) existing.remove();
                        injection.setAttribute('data-injected-id', key);
                        element.appendChild(injection);
                    });
                </script>
        """;
}

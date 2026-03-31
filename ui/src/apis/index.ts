import { axiosInstance } from '@halo-dev/api-client'
import type { CodeSnippet, InjectionRule, ItemList } from '@/types'

const BASE = '/apis/injector.erzbir.com/v1alpha1'
const SNIPPETS = `${BASE}/codeSnippets`
const RULES = `${BASE}/injectionRules`

export const snippetApi = {
  list() {
    return axiosInstance.get<ItemList<CodeSnippet>>(SNIPPETS)
  },

  add(snippet: CodeSnippet) {
    return axiosInstance.post<CodeSnippet>(SNIPPETS, snippet)
  },

  update(id: string, snippet: CodeSnippet) {
    return axiosInstance.put<CodeSnippet>(`${SNIPPETS}/${id}`, snippet)
  },

  delete(id: string) {
    return axiosInstance.delete(`${SNIPPETS}/${id}`)
  },
}

export const ruleApi = {
  list() {
    return axiosInstance.get<ItemList<InjectionRule>>(RULES)
  },

  add(rule: InjectionRule) {
    return axiosInstance.post<InjectionRule>(RULES, rule)
  },

  update(id: string, rule: InjectionRule) {
    return axiosInstance.put<InjectionRule>(`${RULES}/${id}`, rule)
  },

  delete(id: string) {
    return axiosInstance.delete(`${RULES}/${id}`)
  },
}

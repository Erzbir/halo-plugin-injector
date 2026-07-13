import { axiosInstance } from '@halo-dev/api-client'
import type { CodeSnippet, InjectionRule, ItemList } from '@/types'

const BASE = '/apis/injector.erzbir.com/v1alpha1'
const SNIPPETS = `${BASE}/codeSnippets`
const RULES = `${BASE}/injectionRules`

export type ListParams = {
  page?: number
  size?: number
}

export const snippetApi = {
  list(params: ListParams = {}) {
    return axiosInstance.get<ItemList<CodeSnippet>>(SNIPPETS, { params })
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
  list(params: ListParams = {}) {
    return axiosInstance.get<ItemList<InjectionRule>>(RULES, { params })
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

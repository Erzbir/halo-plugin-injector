import type { Metadata } from '@halo-dev/api-client'

export interface CodeSnippet {
  apiVersion: 'injector.erzbir.com/v1alpha1'
  kind: 'CodeSnippet'
  metadata: Metadata
  id: string
  name: string
  code: string
  description: string
  enabled: boolean
  ruleIds: string[]
}

export type InjectionMode = 'HEAD' | 'FOOTER' | 'ID' | 'SELECTOR'
export type InjectionPosition = 'APPEND' | 'PREPEND' | 'BEFORE' | 'AFTER' | 'REPLACE'

export interface PathMatchRule {
  pathPattern: string
}

export interface InjectionRule {
  apiVersion: 'injector.erzbir.com/v1alpha1'
  kind: 'InjectionRule'
  metadata: Metadata
  id: string
  name: string
  description: string
  enabled: boolean
  mode: InjectionMode
  match: string
  position: InjectionPosition
  pathPatterns: PathMatchRule[]
  snippetIds: string[]
}

export interface ItemList<T> {
  page: number
  size: number
  total: number
  items: Array<T>
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
  totalPages: number
}

export type ActiveTab = 'snippets' | 'rules'

export const MODE_OPTIONS: { value: InjectionMode; label: string }[] = [
  { value: 'HEAD', label: 'head' },
  { value: 'FOOTER', label: 'footer' },
  { value: 'ID', label: 'Element ID' },
  { value: 'SELECTOR', label: 'CSS Selector' },
]

export const POSITION_OPTIONS: { value: InjectionPosition; label: string }[] = [
  { value: 'APPEND', label: '内部末尾 (append)' },
  { value: 'PREPEND', label: '内部开头 (prepend)' },
  { value: 'BEFORE', label: '元素之前 (before)' },
  { value: 'AFTER', label: '元素之后 (after)' },
  { value: 'REPLACE', label: '替换元素 (replace)' },
]

export function makeSnippet(override: Partial<CodeSnippet> = {}): CodeSnippet {
  return {
    apiVersion: 'injector.erzbir.com/v1alpha1',
    kind: 'CodeSnippet',
    metadata: { name: '', generateName: 'CodeSnippet-' },
    id: '',
    name: '',
    code: '',
    description: '',
    enabled: true,
    ruleIds: [],
    ...override,
  }
}

export function makeRule(override: Partial<InjectionRule> = {}): InjectionRule {
  return {
    apiVersion: 'injector.erzbir.com/v1alpha1',
    kind: 'InjectionRule',
    metadata: { name: '', generateName: 'InjectionRule-' },
    id: '',
    name: '',
    description: '',
    enabled: true,
    mode: 'FOOTER',
    match: '',
    position: 'APPEND',
    pathPatterns: [{ pathPattern: '/**' }],
    snippetIds: [],
    ...override,
  }
}

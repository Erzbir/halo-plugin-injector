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
export type InjectionPosition = 'APPEND' | 'PREPEND' | 'BEFORE' | 'AFTER' | 'REPLACE' | 'REMOVE'
export type MatchRuleType = 'GROUP' | 'PATH' | 'TEMPLATE_ID'
export type MatchRuleOperator = 'AND' | 'OR'
export type MatchRuleMatcher = 'ANT' | 'REGEX' | 'EXACT'
export type MatchRuleEditorMode = 'SIMPLE' | 'JSON'

export interface MatchRule {
  type: MatchRuleType
  negate: boolean
  operator?: MatchRuleOperator
  matcher?: MatchRuleMatcher
  value?: string
  children?: MatchRule[]
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
  matchRule: MatchRule
  position: InjectionPosition
  snippetIds: string[]
  matchRuleDraft?: string
  matchRuleEditorMode?: MatchRuleEditorMode
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
  { value: 'REMOVE', label: '移除元素 (remove)' },
]

export const MATCH_RULE_GROUP_OPTIONS: { value: MatchRuleOperator; label: string }[] = [
  { value: 'AND', label: '全部满足 (AND)' },
  { value: 'OR', label: '任一满足 (OR)' },
]

export const PATH_MATCHER_OPTIONS: { value: MatchRuleMatcher; label: string }[] = [
  { value: 'ANT', label: 'Ant 风格' },
  { value: 'REGEX', label: '正则表达式' },
  { value: 'EXACT', label: '精确匹配' },
]

export const TEMPLATE_MATCHER_OPTIONS: { value: MatchRuleMatcher; label: string }[] = [
  { value: 'EXACT', label: '精确匹配' },
  { value: 'REGEX', label: '正则表达式' },
]

export function makePathMatchRule(override: Partial<MatchRule> = {}): MatchRule {
  return {
    type: 'PATH',
    negate: false,
    matcher: 'ANT',
    value: '/**',
    ...override,
  }
}

export function makeTemplateMatchRule(override: Partial<MatchRule> = {}): MatchRule {
  return {
    type: 'TEMPLATE_ID',
    negate: false,
    matcher: 'EXACT',
    value: 'post',
    ...override,
  }
}

export function makeMatchRuleGroup(override: Partial<MatchRule> = {}): MatchRule {
  return {
    type: 'GROUP',
    negate: false,
    operator: 'AND',
    children: [makePathMatchRule()],
    ...override,
  }
}

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
    matchRule: makeMatchRuleGroup(),
    position: 'APPEND',
    snippetIds: [],
    ...override,
  }
}

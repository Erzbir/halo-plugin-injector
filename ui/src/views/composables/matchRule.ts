import {
  type InjectionRule,
  type MatchRule,
  makeMatchRuleGroup,
  makePathMatchRule,
  makeTemplateMatchRule,
} from '@/types'

function isObject(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

export function cloneMatchRule(rule: MatchRule): MatchRule {
  return JSON.parse(JSON.stringify(rule)) as MatchRule
}

export function normalizeMatchRule(input: unknown): MatchRule {
  if (!isObject(input)) {
    return makeMatchRuleGroup()
  }

  const type = input.type
  const negate = input.negate === true

  if (type === 'PATH') {
    return makePathMatchRule({
      negate,
      matcher: input.matcher === 'REGEX' || input.matcher === 'EXACT' ? input.matcher : 'ANT',
      value: typeof input.value === 'string' ? input.value : '/**',
    })
  }

  if (type === 'TEMPLATE_ID') {
    return makeTemplateMatchRule({
      negate,
      matcher: input.matcher === 'REGEX' ? 'REGEX' : 'EXACT',
      value: typeof input.value === 'string' ? input.value : 'post',
    })
  }

  const children = Array.isArray(input.children)
    ? input.children.map((child) => normalizeMatchRule(child))
    : [makePathMatchRule()]

  return makeMatchRuleGroup({
    negate,
    operator: input.operator === 'OR' ? 'OR' : 'AND',
    children: children.length ? children : [makePathMatchRule()],
  })
}

export function formatMatchRule(rule: MatchRule): string {
  return JSON.stringify(normalizeMatchRule(rule), null, 2)
}

export function parseMatchRuleDraft(draft?: string | null): MatchRule | null {
  if (!draft || !draft.trim()) {
    return null
  }
  try {
    return normalizeMatchRule(JSON.parse(draft))
  } catch {
    return null
  }
}

export function hydrateRuleForEditor(rule: InjectionRule): InjectionRule {
  const matchRule = normalizeMatchRule(rule.matchRule)
  return {
    ...rule,
    matchRule,
    matchRuleDraft: formatMatchRule(matchRule),
    matchRuleEditorMode: rule.matchRuleEditorMode ?? 'SIMPLE',
  }
}

export function resolveRuleMatchRule(rule: InjectionRule): MatchRule | null {
  if (rule.matchRuleDraft?.trim()) {
    return parseMatchRuleDraft(rule.matchRuleDraft)
  }
  return normalizeMatchRule(rule.matchRule)
}

export function isValidMatchRule(rule: MatchRule | null): boolean {
  if (!rule) return false
  if (rule.type === 'GROUP') {
    return !!rule.children?.length && rule.children.every((child) => isValidMatchRule(child))
  }
  return !!rule.value?.trim()
}

export function makeRulePayload(rule: InjectionRule, snippetIds: string[]) {
  return {
    apiVersion: rule.apiVersion,
    kind: rule.kind,
    metadata: rule.metadata,
    id: rule.id,
    name: rule.name,
    description: rule.description,
    enabled: rule.enabled,
    mode: rule.mode,
    match: rule.match.trim(),
    matchRule: resolveRuleMatchRule(rule) ?? makeMatchRuleGroup(),
    position: rule.position,
    snippetIds,
  }
}

export function matchRuleChips(rule: MatchRule, limit = 4): string[] {
  const chips: string[] = []
  collectMatchRuleChips(normalizeMatchRule(rule), chips, limit)
  return chips
}

function collectMatchRuleChips(rule: MatchRule, chips: string[], limit: number) {
  if (chips.length >= limit) return
  if (rule.type === 'GROUP') {
    rule.children?.forEach((child) => collectMatchRuleChips(child, chips, limit))
    return
  }
  chips.push(describeMatchRule(rule))
}

function describeMatchRule(rule: MatchRule): string {
  const prefix = rule.negate ? '非 ' : ''
  if (rule.type === 'PATH') {
    const matcherLabel =
      rule.matcher === 'REGEX' ? '路径 Regex' : rule.matcher === 'EXACT' ? '路径精确' : '路径 Ant'
    return `${prefix}${matcherLabel}: ${rule.value ?? ''}`
  }
  const matcherLabel = rule.matcher === 'REGEX' ? '模板 Regex' : '模板 ID'
  return `${prefix}${matcherLabel}: ${rule.value ?? ''}`
}

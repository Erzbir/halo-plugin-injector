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

export interface MatchRuleValidationError {
  path: string
  message: string
  line?: number
  column?: number
}

export interface MatchRuleParseResult {
  rule: MatchRule | null
  error: MatchRuleValidationError | null
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

export function parseMatchRuleDraft(draft?: string | null): MatchRuleParseResult {
  if (!draft || !draft.trim()) {
    return {
      rule: null,
      error: {
        path: '$',
        message: '请输入匹配规则 JSON',
      },
    }
  }
  try {
    return validateMatchRuleInput(JSON.parse(draft), '$', true)
  } catch (error) {
    return {
      rule: null,
      error: buildJsonSyntaxError(draft, error),
    }
  }
}

export function validateMatchRuleTree(rule: MatchRule | null | undefined): MatchRuleParseResult {
  if (!rule) {
    return {
      rule: null,
      error: {
        path: '$',
        message: '请完善匹配规则',
      },
    }
  }
  return validateMatchRuleInput(JSON.parse(JSON.stringify(rule)) as unknown, '$', true)
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

export function resolveRuleMatchRule(rule: InjectionRule): MatchRuleParseResult {
  if (rule.matchRuleDraft?.trim()) {
    return parseMatchRuleDraft(rule.matchRuleDraft)
  }
  const normalized = normalizeMatchRule(rule.matchRule)
  return { rule: normalized, error: null }
}

export function isValidMatchRule(rule: MatchRule | null): boolean {
  if (!rule) return false
  if (rule.type === 'GROUP') {
    return !!rule.children?.length && rule.children.every((child) => isValidMatchRule(child))
  }
  return !!rule.value?.trim()
}

export function supportsDomPathPrecheck(rule: MatchRule | null): boolean {
  return analyzePathPrecheckKind(rule) === 'PATH_SCOPED'
}

export function makeRulePayload(rule: InjectionRule, snippetIds: string[]) {
  const result = resolveRuleMatchRule(rule)
  if (!result.rule) {
    return null
  }
  const normalizedSnippetIds = rule.position === 'REMOVE' ? [] : snippetIds
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
    matchRule: result.rule,
    position: rule.position,
    wrapMarker: rule.wrapMarker,
    snippetIds: normalizedSnippetIds,
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
      rule.matcher === 'REGEX' ? '正则表达式' : rule.matcher === 'EXACT' ? '精确匹配' : 'Ant 风格'
    return `${prefix}${matcherLabel}: ${rule.value ?? ''}`
  }
  const matcherLabel = rule.matcher === 'REGEX' ? '正则表达式' : '精确匹配'
  return `${prefix}${matcherLabel}: ${rule.value ?? ''}`
}

type PathPrecheckKind = 'PATH_SCOPED' | 'TEMPLATE_ONLY' | 'UNSUPPORTED'

export function formatMatchRuleError(error: MatchRuleValidationError | null): string {
  if (!error) return ''
  const location =
    typeof error.line === 'number' && typeof error.column === 'number'
      ? `第 ${error.line} 行，第 ${error.column} 列`
      : error.path
  return `${location}：${error.message}`
}

function validateMatchRuleInput(
  input: unknown,
  path: string,
  requireGroupRoot: boolean,
): MatchRuleParseResult {
  if (!isObject(input)) {
    return invalid(path, '必须是对象')
  }

  const type = input.type
  if (type !== 'GROUP' && type !== 'PATH' && type !== 'TEMPLATE_ID') {
    return invalid(`${path}.type`, '仅支持 GROUP、PATH、TEMPLATE_ID')
  }

  if (input.negate !== undefined && typeof input.negate !== 'boolean') {
    return invalid(`${path}.negate`, '必须是布尔值')
  }

  if (requireGroupRoot && type !== 'GROUP') {
    return invalid(`${path}.type`, '根节点必须是 GROUP')
  }

  if (type === 'GROUP') {
    if (input.operator !== undefined && input.operator !== 'AND' && input.operator !== 'OR') {
      return invalid(`${path}.operator`, '仅支持 AND 或 OR')
    }
    if (!Array.isArray(input.children)) {
      return invalid(`${path}.children`, '必须是数组')
    }
    if (!input.children.length) {
      return invalid(`${path}.children`, '至少需要一个子条件')
    }

    const children: MatchRule[] = []
    for (let index = 0; index < input.children.length; index += 1) {
      const childResult = validateMatchRuleInput(
        input.children[index],
        `${path}.children[${index}]`,
        false,
      )
      if (childResult.error) {
        return childResult
      }
      children.push(childResult.rule as MatchRule)
    }

    return {
      rule: makeMatchRuleGroup({
        negate: input.negate === true,
        operator: input.operator === 'OR' ? 'OR' : 'AND',
        children,
      }),
      error: null,
    }
  }

  if (input.operator !== undefined) {
    return invalid(`${path}.operator`, '仅条件组可使用 operator')
  }
  if (input.children !== undefined) {
    return invalid(`${path}.children`, '仅条件组可使用 children')
  }

  if (input.value === undefined || typeof input.value !== 'string' || !input.value.trim()) {
    return invalid(`${path}.value`, '必须是非空字符串')
  }

  if (type === 'PATH') {
    if (
      input.matcher !== undefined &&
      input.matcher !== 'ANT' &&
      input.matcher !== 'REGEX' &&
      input.matcher !== 'EXACT'
    ) {
      return invalid(`${path}.matcher`, '仅支持 ANT、REGEX、EXACT')
    }
    if (input.matcher === 'REGEX') {
      const regexError = validateRegexValue(input.value, `${path}.value`)
      if (regexError) return regexError
    }
    return {
      rule: makePathMatchRule({
        negate: input.negate === true,
        matcher: input.matcher === 'REGEX' || input.matcher === 'EXACT' ? input.matcher : 'ANT',
        value: input.value.trim(),
      }),
      error: null,
    }
  }

  if (input.matcher !== undefined && input.matcher !== 'REGEX' && input.matcher !== 'EXACT') {
    return invalid(`${path}.matcher`, '模板 ID 仅支持 REGEX 或 EXACT')
  }
  if (input.matcher === 'REGEX') {
    const regexError = validateRegexValue(input.value, `${path}.value`)
    if (regexError) return regexError
  }

  return {
    rule: makeTemplateMatchRule({
      negate: input.negate === true,
      matcher: input.matcher === 'REGEX' ? 'REGEX' : 'EXACT',
      value: input.value.trim(),
    }),
    error: null,
  }
}

function analyzePathPrecheckKind(rule: MatchRule | null): PathPrecheckKind {
  if (!rule) return 'UNSUPPORTED'

  if (rule.negate) {
    return analyzeNegatedPathPrecheckKind(rule)
  }

  if (rule.type === 'PATH') {
    return 'PATH_SCOPED'
  }

  if (rule.type === 'TEMPLATE_ID') {
    return 'TEMPLATE_ONLY'
  }

  const children = rule.children ?? []
  if (!children.length) return 'UNSUPPORTED'

  if (rule.operator === 'OR') {
    return analyzeOrPathPrecheckKind(children)
  }

  return analyzeAndPathPrecheckKind(children)
}

function analyzeNegatedPathPrecheckKind(rule: MatchRule): PathPrecheckKind {
  if (rule.type === 'PATH') {
    return 'PATH_SCOPED'
  }
  if (rule.type === 'TEMPLATE_ID') {
    return 'UNSUPPORTED'
  }
  return containsTemplateRule(rule) ? 'UNSUPPORTED' : analyzeGroupPathPrecheckKind(rule)
}

function analyzeGroupPathPrecheckKind(rule: MatchRule): PathPrecheckKind {
  const children = rule.children ?? []
  if (!children.length) return 'UNSUPPORTED'
  return rule.operator === 'OR'
    ? analyzeOrPathPrecheckKind(children)
    : analyzeAndPathPrecheckKind(children)
}

function analyzeAndPathPrecheckKind(children: MatchRule[]): PathPrecheckKind {
  let hasPathScoped = false
  for (const child of children) {
    const kind = analyzePathPrecheckKind(child)
    if (kind === 'UNSUPPORTED') return 'UNSUPPORTED'
    if (kind === 'PATH_SCOPED') hasPathScoped = true
  }
  return hasPathScoped ? 'PATH_SCOPED' : 'TEMPLATE_ONLY'
}

function analyzeOrPathPrecheckKind(children: MatchRule[]): PathPrecheckKind {
  let hasPathScoped = false
  let hasTemplateOnly = false
  for (const child of children) {
    const kind = analyzePathPrecheckKind(child)
    if (kind === 'UNSUPPORTED') return 'UNSUPPORTED'
    if (kind === 'PATH_SCOPED') hasPathScoped = true
    if (kind === 'TEMPLATE_ONLY') hasTemplateOnly = true
  }
  if (hasPathScoped && hasTemplateOnly) return 'UNSUPPORTED'
  if (hasPathScoped) return 'PATH_SCOPED'
  return 'TEMPLATE_ONLY'
}

function containsTemplateRule(rule: MatchRule): boolean {
  if (rule.type === 'TEMPLATE_ID') return true
  if (rule.type !== 'GROUP') return false
  return (rule.children ?? []).some((child) => containsTemplateRule(child))
}

function invalid(path: string, message: string): MatchRuleParseResult {
  return {
    rule: null,
    error: { path, message },
  }
}

function validateRegexValue(value: string, path: string): MatchRuleParseResult | null {
  try {
    // 前端提前编译一次，尽早把错误定位到具体字段，避免用户保存后才发现规则无法生效。
    new RegExp(value)
    return null
  } catch (error) {
    const message = error instanceof Error ? error.message : '正则表达式无效'
    return invalid(path, `正则表达式无效：${message}`)
  }
}

function buildJsonSyntaxError(draft: string, error: unknown): MatchRuleValidationError {
  const message = error instanceof Error ? error.message : 'JSON 格式无效'
  const positionMatch = message.match(/position\s+(\d+)/i)
  if (!positionMatch) {
    return { path: '$', message }
  }

  const position = Number(positionMatch[1])
  const prefix = draft.slice(0, position)
  const lines = prefix.split('\n')
  return {
    path: '$',
    message,
    line: lines.length,
    column: lines[lines.length - 1].length + 1,
  }
}

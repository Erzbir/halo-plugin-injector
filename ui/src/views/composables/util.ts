import { type InjectionRule, MODE_OPTIONS, POSITION_OPTIONS } from '@/types'

export function modeLabel(mode: string) {
  return MODE_OPTIONS.find((o) => o.value === mode)?.label ?? mode
}

export function positionLabel(pos?: string) {
  if (!pos) return ''
  return POSITION_OPTIONS.find((o) => o.value === pos)?.label ?? pos
}
export function rulePreview(rule: InjectionRule) {
  return `${modeLabel(rule.mode)} · ${positionLabel(rule.position)}`
}

export function codePreview(code: string) {
  const t = code.replace(/\s+/g, ' ').trim()
  return t.length > 55 ? t.slice(0, 55) + '...' : t
}

export function uniqueStrings(values: string[]) {
  const seen = new Set<string>()
  return values.map((v) => v.trim()).filter((v) => v && !seen.has(v) && seen.add(v))
}

export function sortSelectedFirst<T extends { id: string }>(items: T[], selectedIds: string[]) {
  const selected = new Set(selectedIds)
  return [...items].sort((a, b) => {
    const aSelected = selected.has(a.id)
    const bSelected = selected.has(b.id)
    if (aSelected === bSelected) return 0
    return aSelected ? -1 : 1
  })
}

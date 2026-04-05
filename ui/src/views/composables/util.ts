import { type InjectionRule, MODE_OPTIONS, POSITION_OPTIONS } from '@/types'

type SortableItem = {
  id: string
  sortOrder?: number
}

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

export function sortBySortOrder<T extends SortableItem>(items: T[]) {
  return items
    .map((item, index) => ({ item, index }))
    .sort((a, b) => {
      const aOrder = Number.isFinite(a.item.sortOrder)
        ? (a.item.sortOrder as number)
        : Number.MAX_SAFE_INTEGER
      const bOrder = Number.isFinite(b.item.sortOrder)
        ? (b.item.sortOrder as number)
        : Number.MAX_SAFE_INTEGER
      if (aOrder !== bOrder) {
        return aOrder - bOrder
      }
      return a.index - b.index
    })
    .map(({ item }) => item)
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

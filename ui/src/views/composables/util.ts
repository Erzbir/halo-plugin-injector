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

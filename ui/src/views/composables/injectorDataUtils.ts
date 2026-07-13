import type { ItemList, MatchRule } from '@/types'
import { uniqueStrings } from './util'

export function emptyList<T>(): ItemList<T> {
  return {
    first: false,
    hasNext: false,
    hasPrevious: false,
    last: false,
    page: 0,
    size: 0,
    totalPages: 0,
    items: [],
    total: 0,
  }
}

export function isValidMatchRule(rule?: MatchRule): boolean {
  if (!rule) return false
  const nodeOperator = rule.operator ?? 'AND'
  if (!['AND', 'OR', 'NOT', 'AND_NOT', 'OR_NOT'].includes(nodeOperator)) return false
  if (rule.type === 'GROUP') {
    const children = rule.children ?? []
    return children.length > 0 && children.every(isValidMatchRule)
  }
  if (!rule.matcher || !rule.value?.trim()) return false
  if (rule.matcher === 'REGEX') {
    try {
      new RegExp(rule.value)
    } catch {
      return false
    }
  }
  if (rule.type === 'PATH') {
    return ['PATH_PATTERN', 'ANT', 'REGEX', 'EXACT'].includes(rule.matcher)
  }
  return false
}

export function cloneValue<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

export function normalizedIds(ids: string[]) {
  return [...uniqueStrings(ids)].sort()
}

export function isSameJson(a: unknown, b: unknown) {
  return JSON.stringify(a) === JSON.stringify(b)
}

export function apiErrorMessage(error: unknown, fallback: string) {
  if (!error || typeof error !== 'object') return fallback
  const responseMessage = (error as { response?: { data?: { message?: unknown } } }).response?.data
    ?.message
  const directMessage = (error as { message?: unknown }).message
  const detail =
    typeof responseMessage === 'string'
      ? responseMessage.trim()
      : typeof directMessage === 'string'
        ? directMessage.trim()
        : ''
  return detail ? `${fallback}: ${detail}` : fallback
}

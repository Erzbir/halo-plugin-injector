import { ruleApi } from '@/apis'
import type { InjectionRule } from '@/types'
import { uniqueStrings } from './util'

type UpdateRule = (id: string, rule: InjectionRule) => Promise<unknown>
type RuleChange = { before: InjectionRule; after: InjectionRule }

async function applyRuleChanges(changes: RuleChange[], updateRule: UpdateRule) {
  const results = await Promise.allSettled(changes.map(({ after }) => updateRule(after.id, after)))
  const failures = results.filter((result) => result.status === 'rejected')
  if (!failures.length) return

  await Promise.allSettled(
    changes.flatMap(({ before }, index) =>
      results[index]?.status === 'fulfilled' ? [updateRule(before.id, before)] : [],
    ),
  )
  throw new Error('Failed to synchronize snippet-rule relations')
}

export function ruleIdsForSnippet(rules: InjectionRule[], snippetId: string) {
  return rules.filter((rule) => rule.snippetIds?.includes(snippetId)).map((rule) => rule.id)
}

export async function syncSnippetRuleRelations(
  snippetId: string,
  nextRuleIds: string[],
  rules: InjectionRule[],
  updateRule: UpdateRule = ruleApi.update,
) {
  const next = new Set(uniqueStrings(nextRuleIds))
  const changes = rules.flatMap((rule) => {
    const has = rule.snippetIds?.includes(snippetId) ?? false
    const shouldHave = next.has(rule.id)
    if (has === shouldHave) return []
    const snippetIds = shouldHave
      ? uniqueStrings([...(rule.snippetIds ?? []), snippetId])
      : (rule.snippetIds ?? []).filter((id) => id !== snippetId)
    return [{ before: rule, after: { ...rule, snippetIds } }]
  })
  await applyRuleChanges(changes, updateRule)
}

export async function detachSnippetsFromRules(
  snippetIds: string[],
  rules: InjectionRule[],
  updateRule: UpdateRule = ruleApi.update,
) {
  const removed = new Set(uniqueStrings(snippetIds))
  const changes = rules
    .filter((rule) => rule.snippetIds?.some((id) => removed.has(id)))
    .map((rule) => ({
      before: rule,
      after: {
        ...rule,
        snippetIds: (rule.snippetIds ?? []).filter((id) => !removed.has(id)),
      },
    }))
  await applyRuleChanges(changes, updateRule)
}

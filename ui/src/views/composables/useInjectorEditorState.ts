import { computed, ref, watch, type ComputedRef } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { cloneValue, isSameJson, normalizedIds } from './injectorDataUtils'
import { ruleIdsForSnippet } from './injectorRelations'

export function useInjectorEditorState(
  snippets: ComputedRef<CodeSnippet[]>,
  rules: ComputedRef<InjectionRule[]>,
) {
  const selectedSnippetId = ref<string | null>(null)
  const selectedRuleId = ref<string | null>(null)

  const editSnippet = ref<CodeSnippet | null>(null)
  const editSnippetRuleIds = ref<string[]>([])
  const originalSnippet = ref<CodeSnippet | null>(null)
  const originalSnippetRuleIds = ref<string[]>([])

  const editRule = ref<InjectionRule | null>(null)
  const editRuleSnippetIds = ref<string[]>([])
  const originalRule = ref<InjectionRule | null>(null)
  const originalRuleSnippetIds = ref<string[]>([])

  const snippetDirty = ref(false)
  const ruleDirty = ref(false)

  function refreshDirty() {
    if (selectedSnippetId.value && editSnippet.value && originalSnippet.value) {
      const sameSnippet = isSameJson(editSnippet.value, originalSnippet.value)
      const sameRelations = isSameJson(
        normalizedIds(editSnippetRuleIds.value),
        normalizedIds(originalSnippetRuleIds.value),
      )
      snippetDirty.value = !(sameSnippet && sameRelations)
    } else {
      snippetDirty.value = false
    }

    if (selectedRuleId.value && editRule.value && originalRule.value) {
      const sameRule = isSameJson(editRule.value, originalRule.value)
      const sameRelations = isSameJson(
        normalizedIds(editRuleSnippetIds.value),
        normalizedIds(originalRuleSnippetIds.value),
      )
      ruleDirty.value = !(sameRule && sameRelations)
    } else {
      ruleDirty.value = false
    }
  }

  const rulesUsingSnippet = computed(() => {
    if (!selectedSnippetId.value) return []
    return rules.value.filter((rule) => rule.snippetIds?.includes(selectedSnippetId.value!))
  })

  const snippetsInRule = computed(() => {
    if (!selectedRuleId.value) return []
    const rule = rules.value.find((item) => item.id === selectedRuleId.value)
    if (!rule?.snippetIds?.length) return []
    return rule.snippetIds
      .map((id) => snippets.value.find((snippet) => snippet.id === id))
      .filter((snippet): snippet is CodeSnippet => !!snippet)
  })

  function syncEditSnippet() {
    if (!selectedSnippetId.value) {
      editSnippet.value = null
      originalSnippet.value = null
      editSnippetRuleIds.value = []
      originalSnippetRuleIds.value = []
      snippetDirty.value = false
      return
    }
    const found = snippets.value.find((snippet) => snippet.id === selectedSnippetId.value)
    editSnippet.value = found ? cloneValue(found) : null
    originalSnippet.value = found ? cloneValue(found) : null
    editSnippetRuleIds.value = ruleIdsForSnippet(rules.value, selectedSnippetId.value)
    originalSnippetRuleIds.value = cloneValue(editSnippetRuleIds.value)
    refreshDirty()
  }

  function syncEditRule() {
    if (!selectedRuleId.value) {
      editRule.value = null
      originalRule.value = null
      editRuleSnippetIds.value = []
      originalRuleSnippetIds.value = []
      ruleDirty.value = false
      return
    }
    const found = rules.value.find((rule) => rule.id === selectedRuleId.value)
    editRule.value = found ? cloneValue(found) : null
    originalRule.value = found ? cloneValue(found) : null
    editRuleSnippetIds.value = found ? [...(found.snippetIds ?? [])] : []
    originalRuleSnippetIds.value = cloneValue(editRuleSnippetIds.value)
    refreshDirty()
  }

  watch(selectedSnippetId, syncEditSnippet)
  watch(selectedRuleId, syncEditRule)

  function toggleRuleInSnippetEditor(ruleId: string) {
    const ids = editSnippetRuleIds.value
    editSnippetRuleIds.value = ids.includes(ruleId)
      ? ids.filter((id) => id !== ruleId)
      : [...ids, ruleId]
    refreshDirty()
  }

  function toggleSnippetInRuleEditor(snippetId: string) {
    const ids = editRuleSnippetIds.value
    editRuleSnippetIds.value = ids.includes(snippetId)
      ? ids.filter((id) => id !== snippetId)
      : [...ids, snippetId]
    refreshDirty()
  }

  function updateEditSnippet(nextSnippet: CodeSnippet) {
    editSnippet.value = nextSnippet
    refreshDirty()
  }

  function updateEditRule(nextRule: InjectionRule) {
    editRule.value = nextRule
    refreshDirty()
  }

  function revertSnippetField(field: keyof CodeSnippet | 'ruleIds') {
    if (!originalSnippet.value || !editSnippet.value) return
    if (field === 'ruleIds') {
      editSnippetRuleIds.value = cloneValue(originalSnippetRuleIds.value)
    } else {
      editSnippet.value = {
        ...editSnippet.value,
        [field]: cloneValue(originalSnippet.value[field]),
      }
    }
    refreshDirty()
  }

  function revertRuleField(field: keyof InjectionRule | 'snippetIds') {
    if (!originalRule.value || !editRule.value) return
    if (field === 'snippetIds') {
      editRuleSnippetIds.value = cloneValue(originalRuleSnippetIds.value)
    } else {
      editRule.value = {
        ...editRule.value,
        [field]: cloneValue(originalRule.value[field]),
      }
    }
    refreshDirty()
  }

  function revertSnippetAll() {
    if (!originalSnippet.value) return
    editSnippet.value = cloneValue(originalSnippet.value)
    editSnippetRuleIds.value = cloneValue(originalSnippetRuleIds.value)
    refreshDirty()
  }

  function revertRuleAll() {
    if (!originalRule.value) return
    editRule.value = cloneValue(originalRule.value)
    editRuleSnippetIds.value = cloneValue(originalRuleSnippetIds.value)
    refreshDirty()
  }

  function isSnippetFieldDirty(field: keyof CodeSnippet | 'ruleIds') {
    if (!editSnippet.value || !originalSnippet.value) return false
    if (field === 'ruleIds') {
      return !isSameJson(
        normalizedIds(editSnippetRuleIds.value),
        normalizedIds(originalSnippetRuleIds.value),
      )
    }
    return !isSameJson(editSnippet.value[field], originalSnippet.value[field])
  }

  function isRuleFieldDirty(field: keyof InjectionRule | 'snippetIds') {
    if (!editRule.value || !originalRule.value) return false
    if (field === 'snippetIds') {
      return !isSameJson(
        normalizedIds(editRuleSnippetIds.value),
        normalizedIds(originalRuleSnippetIds.value),
      )
    }
    return !isSameJson(editRule.value[field], originalRule.value[field])
  }

  return {
    selectedSnippetId,
    selectedRuleId,
    editSnippet,
    editSnippetRuleIds,
    originalSnippet,
    editRule,
    editRuleSnippetIds,
    originalRule,
    snippetDirty,
    ruleDirty,
    refreshDirty,
    rulesUsingSnippet,
    snippetsInRule,
    syncEditSnippet,
    syncEditRule,
    toggleRuleInSnippetEditor,
    toggleSnippetInRuleEditor,
    updateEditSnippet,
    updateEditRule,
    revertSnippetField,
    revertRuleField,
    revertSnippetAll,
    revertRuleAll,
    isSnippetFieldDirty,
    isRuleFieldDirty,
  }
}

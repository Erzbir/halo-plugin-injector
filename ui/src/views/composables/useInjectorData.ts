import { computed, ref } from 'vue'
import { Dialog, Toast } from '@halo-dev/components'
import { ruleApi, snippetApi } from '@/apis'
import type { CodeSnippet, InjectionRule, ItemList } from '@/types'
import { uniqueStrings } from './util'
import { emptyList, isValidMatchRule } from './injectorDataUtils'
import {
  detachSnippetsFromRules,
  restoreDetachedSnippetRelations,
  syncSnippetRuleRelations,
} from './injectorRelations'
import { useInjectorEditorState } from './useInjectorEditorState'

export type BatchActionResult = {
  succeededIds: string[]
  failedIds: string[]
}

function emptyBatchResult(): BatchActionResult {
  return { succeededIds: [], failedIds: [] }
}

async function settleById(
  ids: string[],
  action: (id: string) => Promise<unknown>,
): Promise<BatchActionResult> {
  const results = await Promise.allSettled(ids.map((id) => action(id)))
  return results.reduce<BatchActionResult>((result, item, index) => {
    const id = ids[index]
    if (!id) return result
    if (item.status === 'fulfilled') result.succeededIds.push(id)
    else result.failedIds.push(id)
    return result
  }, emptyBatchResult())
}

function showBatchResult(result: BatchActionResult, action: string) {
  if (!result.failedIds.length) {
    Toast.success(`${action}成功, 共 ${result.succeededIds.length} 项`)
    return
  }
  Toast.error(
    `${action}完成, 成功 ${result.succeededIds.length} 项, 失败 ${result.failedIds.length} 项`,
  )
}

export function useInjectorData() {
  const loading = ref(false)
  const saving = ref(false)
  const snippetsResp = ref<ItemList<CodeSnippet>>(emptyList())
  const rulesResp = ref<ItemList<InjectionRule>>(emptyList())
  const snippets = computed(() => snippetsResp.value.items)
  const rules = computed(() => rulesResp.value.items)

  const editor = useInjectorEditorState(snippets, rules)
  const {
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
  } = editor

  function setSnippetsItems(items: CodeSnippet[]) {
    snippetsResp.value = { ...snippetsResp.value, items }
  }

  function setRulesItems(items: InjectionRule[]) {
    rulesResp.value = { ...rulesResp.value, items }
  }

  function validateRule(rule: InjectionRule): string | null {
    if (!isValidMatchRule(rule.matchRule)) return '匹配规则无效, 请完善规则组'
    if ((rule.mode === 'SELECTOR' || rule.mode === 'ID') && !rule.match.trim()) {
      return '请填写匹配内容'
    }
    return null
  }

  async function fetchAll() {
    loading.value = true
    try {
      const [snippetResponse, ruleResponse] = await Promise.all([snippetApi.list(), ruleApi.list()])
      snippetsResp.value = snippetResponse.data
      rulesResp.value = ruleResponse.data
      syncEditSnippet()
      syncEditRule()
    } catch {
      Toast.error('加载数据失败')
    } finally {
      loading.value = false
    }
  }

  async function addSnippet(snippet: CodeSnippet, ruleIds: string[]): Promise<string | null> {
    if (!snippet.code.trim()) {
      Toast.error('代码内容不能为空')
      return null
    }
    const nextRuleIds = uniqueStrings(ruleIds)
    saving.value = true
    try {
      // ruleIds is retained in the schema for compatibility. InjectionRule.snippetIds is canonical.
      const response = await snippetApi.add({ ...snippet, ruleIds: [] })
      const id = response.data.id
      try {
        await syncSnippetRuleRelations(id, nextRuleIds, rules.value)
      } catch (error) {
        await snippetApi.delete(id).catch(() => undefined)
        throw error
      }
      await fetchAll()
      selectedSnippetId.value = id
      Toast.success('代码片段已创建')
      return id
    } catch {
      Toast.error('创建失败')
      return null
    } finally {
      saving.value = false
    }
  }

  async function addRule(rule: InjectionRule, snippetIds: string[]): Promise<string | null> {
    const errorMessage = validateRule(rule)
    if (errorMessage) {
      Toast.error(errorMessage)
      return null
    }
    saving.value = true
    try {
      const response = await ruleApi.add({
        ...rule,
        snippetIds: uniqueStrings(snippetIds),
      })
      await fetchAll()
      selectedRuleId.value = response.data.id
      Toast.success('规则已创建')
      return response.data.id
    } catch {
      Toast.error('创建失败')
      return null
    } finally {
      saving.value = false
    }
  }

  async function saveSnippet(): Promise<boolean> {
    if (!editSnippet.value?.code.trim()) {
      Toast.error('代码内容不能为空')
      return false
    }
    const snippet = editSnippet.value
    const previousSnippet = originalSnippet.value
    saving.value = true
    try {
      await snippetApi.update(snippet.id, { ...snippet, ruleIds: [] })
      try {
        await syncSnippetRuleRelations(snippet.id, editSnippetRuleIds.value, rules.value)
      } catch (error) {
        if (previousSnippet) {
          await snippetApi.update(snippet.id, previousSnippet).catch(() => undefined)
        }
        throw error
      }
      await fetchAll()
      Toast.success('保存成功')
      return true
    } catch {
      await fetchAll()
      Toast.error('保存失败')
      return false
    } finally {
      saving.value = false
    }
  }

  async function saveRule(): Promise<boolean> {
    if (!editRule.value) return false
    const errorMessage = validateRule(editRule.value)
    if (errorMessage) {
      Toast.error(errorMessage)
      return false
    }
    saving.value = true
    try {
      await ruleApi.update(editRule.value.id, {
        ...editRule.value,
        snippetIds: uniqueStrings(editRuleSnippetIds.value),
      })
      await fetchAll()
      Toast.success('保存成功')
      return true
    } catch {
      Toast.error('保存失败')
      return false
    } finally {
      saving.value = false
    }
  }

  async function setSnippetEnabled(enabled: boolean) {
    if (!editSnippet.value) return
    const persisted = snippets.value.find((snippet) => snippet.id === editSnippet.value?.id)
    if (!persisted) return
    if (persisted.enabled === enabled) return
    saving.value = true
    try {
      await snippetApi.update(persisted.id, { ...persisted, enabled })
      setSnippetsItems(
        snippets.value.map((snippet) =>
          snippet.id === persisted.id ? { ...snippet, enabled } : snippet,
        ),
      )
      editSnippet.value = { ...editSnippet.value, enabled }
      if (originalSnippet.value) originalSnippet.value = { ...originalSnippet.value, enabled }
      refreshDirty()
    } catch {
      Toast.error('操作失败')
    } finally {
      saving.value = false
    }
  }

  async function setRuleEnabled(enabled: boolean) {
    if (!editRule.value) return
    const persisted = rules.value.find((rule) => rule.id === editRule.value?.id)
    if (!persisted) return
    if (persisted.enabled === enabled) return
    saving.value = true
    try {
      await ruleApi.update(persisted.id, { ...persisted, enabled })
      setRulesItems(
        rules.value.map((rule) => (rule.id === persisted.id ? { ...rule, enabled } : rule)),
      )
      editRule.value = { ...editRule.value, enabled }
      if (originalRule.value) originalRule.value = { ...originalRule.value, enabled }
      refreshDirty()
    } catch {
      Toast.error('操作失败')
    } finally {
      saving.value = false
    }
  }

  function confirmDeleteSnippet() {
    if (!editSnippet.value) return
    const id = editSnippet.value.id
    Dialog.warning({
      title: '删除代码片段',
      description: `确认删除代码片段 ${id}? 删除后无法恢复`,
      confirmType: 'danger',
      async onConfirm() {
        let detached = false
        try {
          await detachSnippetsFromRules([id], rules.value)
          detached = true
          await snippetApi.delete(id)
          setSnippetsItems(snippets.value.filter((snippet) => snippet.id !== id))
          setRulesItems(
            rules.value.map((rule) => ({
              ...rule,
              snippetIds: (rule.snippetIds ?? []).filter((snippetId) => snippetId !== id),
            })),
          )
          if (selectedSnippetId.value === id) selectedSnippetId.value = null
          syncEditSnippet()
          syncEditRule()
          Toast.success('代码片段已删除')
        } catch {
          if (detached) {
            try {
              await restoreDetachedSnippetRelations([id], [id], rules.value)
            } catch {
              await fetchAll()
              Toast.error('删除失败, 关联恢复失败, 请检查相关规则')
              return
            }
          }
          await fetchAll()
          Toast.error(detached ? '删除失败, 原关联已恢复' : '删除失败')
        }
      },
    })
  }

  function confirmDeleteRule() {
    if (!editRule.value) return
    const id = editRule.value.id
    Dialog.warning({
      title: '删除规则',
      description: `确认删除规则 ${id}? 删除后无法恢复`,
      confirmType: 'danger',
      async onConfirm() {
        try {
          await ruleApi.delete(id)
          setRulesItems(rules.value.filter((rule) => rule.id !== id))
          if (selectedRuleId.value === id) selectedRuleId.value = null
          syncEditRule()
          syncEditSnippet()
          Toast.success('规则已删除')
        } catch {
          Toast.error('删除失败')
        }
      },
    })
  }

  async function batchSetSnippetEnabled(
    ids: string[],
    enabled: boolean,
  ): Promise<BatchActionResult> {
    const targets = new Set(uniqueStrings(ids))
    if (!targets.size) return emptyBatchResult()
    const targetSnippets = snippets.value.filter((snippet) => targets.has(snippet.id))
    saving.value = true
    try {
      const snippetById = new Map(targetSnippets.map((snippet) => [snippet.id, snippet]))
      const result = await settleById(
        targetSnippets.map((snippet) => snippet.id),
        (id) => snippetApi.update(id, { ...snippetById.get(id)!, enabled }),
      )
      await fetchAll()
      showBatchResult(result, enabled ? '批量启用' : '批量禁用')
      return result
    } finally {
      saving.value = false
    }
  }

  async function batchSetRuleEnabled(
    ids: string[],
    enabled: boolean,
  ): Promise<BatchActionResult> {
    const targets = new Set(uniqueStrings(ids))
    if (!targets.size) return emptyBatchResult()
    const targetRules = rules.value.filter((rule) => targets.has(rule.id))
    saving.value = true
    try {
      const ruleById = new Map(targetRules.map((rule) => [rule.id, rule]))
      const result = await settleById(
        targetRules.map((rule) => rule.id),
        (id) => ruleApi.update(id, { ...ruleById.get(id)!, enabled }),
      )
      await fetchAll()
      showBatchResult(result, enabled ? '批量启用' : '批量禁用')
      return result
    } finally {
      saving.value = false
    }
  }

  async function batchDeleteSnippets(ids: string[]): Promise<BatchActionResult> {
    const targets = uniqueStrings(ids)
    if (!targets.length) return emptyBatchResult()
    saving.value = true
    try {
      await detachSnippetsFromRules(targets, rules.value)
      const result = await settleById(targets, snippetApi.delete)
      if (result.failedIds.length) {
        try {
          await restoreDetachedSnippetRelations(targets, result.failedIds, rules.value)
        } catch {
          await fetchAll()
          Toast.error(
            `批量删除完成, 成功 ${result.succeededIds.length} 项, 失败 ${result.failedIds.length} 项, 部分关联恢复失败`,
          )
          return result
        }
      }
      const succeeded = new Set(result.succeededIds)
      setSnippetsItems(snippets.value.filter((snippet) => !succeeded.has(snippet.id)))
      if (selectedSnippetId.value && succeeded.has(selectedSnippetId.value)) {
        selectedSnippetId.value = null
      }
      await fetchAll()
      syncEditSnippet()
      syncEditRule()
      showBatchResult(result, '批量删除')
      return result
    } catch {
      await fetchAll()
      Toast.error('批量删除失败')
      return { succeededIds: [], failedIds: targets }
    } finally {
      saving.value = false
    }
  }

  async function batchDeleteRules(ids: string[]): Promise<BatchActionResult> {
    const targets = uniqueStrings(ids)
    if (!targets.length) return emptyBatchResult()
    saving.value = true
    try {
      const result = await settleById(targets, ruleApi.delete)
      const succeeded = new Set(result.succeededIds)
      setRulesItems(rules.value.filter((rule) => !succeeded.has(rule.id)))
      if (selectedRuleId.value && succeeded.has(selectedRuleId.value)) {
        selectedRuleId.value = null
      }
      await fetchAll()
      syncEditRule()
      syncEditSnippet()
      showBatchResult(result, '批量删除')
      return result
    } finally {
      saving.value = false
    }
  }

  return {
    loading,
    saving,
    snippets,
    rules,
    selectedSnippetId,
    selectedRuleId,
    editSnippet,
    editSnippetRuleIds,
    editRule,
    editRuleSnippetIds,
    snippetDirty,
    ruleDirty,
    rulesUsingSnippet,
    snippetsInRule,
    fetchAll,
    addSnippet,
    saveSnippet,
    setSnippetEnabled,
    confirmDeleteSnippet,
    toggleRuleInSnippetEditor: editor.toggleRuleInSnippetEditor,
    updateEditSnippet: editor.updateEditSnippet,
    revertSnippetField: editor.revertSnippetField,
    revertSnippetAll: editor.revertSnippetAll,
    isSnippetFieldDirty: editor.isSnippetFieldDirty,
    addRule,
    saveRule,
    setRuleEnabled,
    confirmDeleteRule,
    toggleSnippetInRuleEditor: editor.toggleSnippetInRuleEditor,
    updateEditRule: editor.updateEditRule,
    revertRuleField: editor.revertRuleField,
    revertRuleAll: editor.revertRuleAll,
    isRuleFieldDirty: editor.isRuleFieldDirty,
    batchSetSnippetEnabled,
    batchSetRuleEnabled,
    batchDeleteSnippets,
    batchDeleteRules,
  }
}

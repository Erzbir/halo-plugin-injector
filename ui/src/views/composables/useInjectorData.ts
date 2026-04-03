import { computed, ref, watch } from 'vue'
import { Dialog, Toast } from '@halo-dev/components'
import { ruleApi, snippetApi } from '@/apis'
import type { CodeSnippet, InjectionRule, ItemList } from '@/types'
import { uniqueStrings } from './util'

function emptyList<T>(): ItemList<T> {
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

export function useInjectorData() {
  const loading = ref(false)
  const saving = ref(false)

  const snippetsResp = ref<ItemList<CodeSnippet>>(emptyList())
  const rulesResp = ref<ItemList<InjectionRule>>(emptyList())

  const snippets = computed(() => snippetsResp.value.items)
  const rules = computed(() => rulesResp.value.items)

  const selectedSnippetId = ref<string | null>(null)
  const selectedRuleId = ref<string | null>(null)

  const editSnippet = ref<CodeSnippet | null>(null)
  const editSnippetRuleIds = ref<string[]>([])

  const editRule = ref<InjectionRule | null>(null)

  const editDirty = ref(false)

  const rulesUsingSnippet = computed(() => {
    if (!selectedSnippetId.value) return []
    return rules.value.filter((r) => r.snippetIds?.includes(selectedSnippetId.value!))
  })

  const snippetsInRule = computed(() => {
    if (!selectedRuleId.value) return []
    const rule = rules.value.find((r) => r.id === selectedRuleId.value)
    if (!rule?.snippetIds?.length) return []
    return rule.snippetIds
      .map((id) => snippets.value.find((s) => s.id === id))
      .filter((s): s is CodeSnippet => !!s)
  })

  async function fetchAll() {
    loading.value = true
    try {
      const [sr, rr] = await Promise.all([snippetApi.list(), ruleApi.list()])
      snippetsResp.value = sr.data
      rulesResp.value = rr.data
      _syncEditSnippet()
      _syncEditRule()
    } catch {
      Toast.error('加载数据失败')
    } finally {
      loading.value = false
    }
  }

  function _syncEditSnippet() {
    if (!selectedSnippetId.value) {
      editSnippet.value = null
      editSnippetRuleIds.value = []
      editDirty.value = false
      return
    }
    const found = snippets.value.find((s) => s.id === selectedSnippetId.value)
    editSnippet.value = found ? JSON.parse(JSON.stringify(found)) : null
    editSnippetRuleIds.value = rules.value
      .filter((r) => r.snippetIds?.includes(selectedSnippetId.value!))
      .map((r) => r.id)
    editDirty.value = false
  }

  function _syncEditRule() {
    if (!selectedRuleId.value) {
      editRule.value = null
      editDirty.value = false
      return
    }
    const found = rules.value.find((r) => r.id === selectedRuleId.value)
    editRule.value = found ? JSON.parse(JSON.stringify(found)) : null
    editDirty.value = false
  }

  watch(selectedSnippetId, _syncEditSnippet)
  watch(selectedRuleId, _syncEditRule)

  async function addSnippet(snippet: CodeSnippet, ruleIds: string[]): Promise<string | null> {
    if (!snippet.code.trim()) {
      Toast.error('代码内容不能为空')
      return null
    }
    saving.value = true
    try {
      const res = await snippetApi.add(snippet)
      const id = res.data.id
      if (ruleIds.length) await _applySnippetRuleSelection(id, ruleIds)
      await fetchAll()
      selectedSnippetId.value = id
      Toast.success('代码块已创建')
      return id
    } catch {
      Toast.error('创建失败')
      return null
    } finally {
      saving.value = false
    }
  }

  async function saveSnippet() {
    if (!editSnippet.value?.code.trim()) {
      Toast.error('代码内容不能为空')
      return
    }
    saving.value = true
    try {
      await snippetApi.update(editSnippet.value.id, editSnippet.value)
      await _applySnippetRuleSelection(editSnippet.value.id, editSnippetRuleIds.value)
      await fetchAll()
      editDirty.value = false
      Toast.success('保存成功')
    } catch {
      Toast.error('保存失败')
    } finally {
      saving.value = false
    }
  }

  async function toggleSnippetEnabled() {
    if (!editSnippet.value) return
    try {
      editSnippet.value.enabled = !editSnippet.value.enabled
      await snippetApi.update(editSnippet.value.id, editSnippet.value)
      await fetchAll()
    } catch {
      Toast.error('操作失败')
    }
  }

  function confirmDeleteSnippet() {
    if (!editSnippet.value) return
    const id = editSnippet.value.id
    Dialog.warning({
      title: '删除代码块',
      description: `确认删除代码块 ${id}？删除后无法恢复。`,
      confirmType: 'danger',
      async onConfirm() {
        try {
          await snippetApi.delete(id)
          snippetsResp.value.items = snippetsResp.value.items.filter((s) => s.id !== id)

          rulesResp.value.items = rulesResp.value.items.map((r) => ({
            ...r,
            snippetIds: (r.snippetIds ?? []).filter((sid) => sid !== id),
          }))
          if (selectedSnippetId.value === id) selectedSnippetId.value = null
          editSnippet.value = null
          editSnippetRuleIds.value = []
          editDirty.value = false
          Toast.success('代码块已删除')
        } catch {
          Toast.error('删除失败')
        }
      },
    })
  }

  function toggleRuleInSnippetEditor(ruleId: string) {
    const ids = editSnippetRuleIds.value
    editSnippetRuleIds.value = ids.includes(ruleId)
      ? ids.filter((id) => id !== ruleId)
      : [...ids, ruleId]
    editDirty.value = true
  }

  function _validateRule(rule: InjectionRule): string | null {
    const pats = uniqueStrings(rule.pathPatterns.map((p) => p.pathPattern))
    if (!pats.length) return '至少需要一条路径规则'
    if ((rule.mode === 'SELECTOR' || rule.mode === 'ID') && !rule.match.trim())
      return '请填写匹配内容'
    return null
  }

  async function addRule(rule: InjectionRule): Promise<string | null> {
    const err = _validateRule(rule)
    if (err) {
      Toast.error(err)
      return null
    }
    rule.pathPatterns = uniqueStrings(rule.pathPatterns.map((p) => p.pathPattern)).map((p) => ({
      pathPattern: p,
    }))
    saving.value = true
    try {
      const res = await ruleApi.add(rule)
      await fetchAll()
      selectedRuleId.value = res.data.id
      Toast.success('规则已创建')
      return res.data.id
    } catch {
      Toast.error('创建失败')
      return null
    } finally {
      saving.value = false
    }
  }

  async function saveRule() {
    if (!editRule.value) return
    const err = _validateRule(editRule.value)
    if (err) {
      Toast.error(err)
      return
    }
    editRule.value.pathPatterns = uniqueStrings(
      editRule.value.pathPatterns.map((p) => p.pathPattern),
    ).map((p) => ({ pathPattern: p }))
    saving.value = true
    try {
      await ruleApi.update(editRule.value.id, editRule.value)
      await fetchAll()
      editDirty.value = false
      Toast.success('保存成功')
    } catch {
      Toast.error('保存失败')
    } finally {
      saving.value = false
    }
  }

  async function toggleRuleEnabled() {
    if (!editRule.value) return
    try {
      editRule.value.enabled = !editRule.value.enabled
      await ruleApi.update(editRule.value.id, editRule.value)
      await fetchAll()
    } catch {
      Toast.error('操作失败')
    }
  }

  function confirmDeleteRule() {
    if (!editRule.value) return
    const id = editRule.value.id
    Dialog.warning({
      title: '删除规则',
      description: `确认删除规则 ${id}？删除后无法恢复。`,
      confirmType: 'danger',
      async onConfirm() {
        try {
          await ruleApi.delete(id)
          rulesResp.value.items = rulesResp.value.items.filter((r) => r.id !== id)
          if (selectedRuleId.value === id) selectedRuleId.value = null
          editRule.value = null
          editDirty.value = false
          Toast.success('规则已删除')
        } catch {
          Toast.error('删除失败')
        }
      },
    })
  }

  function toggleSnippetInRuleEditor(snippetId: string) {
    if (!editRule.value) return
    const ids = editRule.value.snippetIds ?? []
    editRule.value.snippetIds = ids.includes(snippetId)
      ? ids.filter((n) => n !== snippetId)
      : [...ids, snippetId]
    editDirty.value = true
  }

  async function _applySnippetRuleSelection(snippetId: string, nextRuleIds: string[]) {
    const next = new Set(nextRuleIds)
    const current = new Set(
      rules.value.filter((r) => r.snippetIds?.includes(snippetId)).map((r) => r.id),
    )
    await Promise.all(
      rules.value.map(async (rule) => {
        const has = current.has(rule.id)
        const shouldHave = next.has(rule.id)
        if (has === shouldHave) return
        const updatedIds = shouldHave
          ? [...(rule.snippetIds ?? []), snippetId]
          : (rule.snippetIds ?? []).filter((id) => id !== snippetId)
        await ruleApi.update(rule.id, { ...rule, snippetIds: updatedIds })
      }),
    )
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
    editDirty,
    rulesUsingSnippet,
    snippetsInRule,
    fetchAll,
    addSnippet,
    saveSnippet,
    toggleSnippetEnabled,
    confirmDeleteSnippet,
    toggleRuleInSnippetEditor,
    addRule,
    saveRule,
    toggleRuleEnabled,
    confirmDeleteRule,
    toggleSnippetInRuleEditor,
  }
}

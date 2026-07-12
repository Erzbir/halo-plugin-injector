import { computed, ref, watch } from 'vue'
import { Dialog, Toast } from '@halo-dev/components'
import { ruleApi, snippetApi } from '@/apis'
import type { CodeSnippet, InjectionRule, ItemList, MatchRule } from '@/types'
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

function isValidMatchRule(rule?: MatchRule): boolean {
  if (!rule) return false
  const nodeOperator = rule.operator ?? 'AND'
  if (
    nodeOperator !== 'AND' &&
    nodeOperator !== 'OR' &&
    nodeOperator !== 'NOT' &&
    nodeOperator !== 'AND_NOT' &&
    nodeOperator !== 'OR_NOT'
  )
    return false
  if (rule.type === 'GROUP') {
    const children = rule.children ?? []
    return children.length > 0 && children.every((child) => isValidMatchRule(child))
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
    return (
      rule.matcher === 'PATH_PATTERN' ||
      rule.matcher === 'ANT' ||
      rule.matcher === 'REGEX' ||
      rule.matcher === 'EXACT'
    )
  }
  if (rule.type === 'TEMPLATE_ID') {
    return rule.matcher === 'EXACT' || rule.matcher === 'REGEX'
  }
  return false
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
  const originalSnippet = ref<CodeSnippet | null>(null)
  const originalSnippetRuleIds = ref<string[]>([])

  const editRule = ref<InjectionRule | null>(null)
  const editRuleSnippetIds = ref<string[]>([])
  const originalRule = ref<InjectionRule | null>(null)
  const originalRuleSnippetIds = ref<string[]>([])

  const snippetDirty = ref(false)
  const ruleDirty = ref(false)

  function cloneValue<T>(value: T): T {
    return JSON.parse(JSON.stringify(value)) as T
  }

  function normalizedIds(ids: string[]) {
    return [...uniqueStrings(ids)].sort()
  }

  function isSameJson(a: unknown, b: unknown) {
    return JSON.stringify(a) === JSON.stringify(b)
  }

  function setSnippetsItems(items: CodeSnippet[]) {
    snippetsResp.value = { ...snippetsResp.value, items }
  }

  function setRulesItems(items: InjectionRule[]) {
    rulesResp.value = { ...rulesResp.value, items }
  }

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

  function _validateRule(rule: InjectionRule): string | null {
    if (!isValidMatchRule(rule.matchRule)) return '匹配规则无效, 请完善规则组'
    if ((rule.mode === 'SELECTOR' || rule.mode === 'ID') && !rule.match.trim())
      return '请填写匹配内容'
    return null
  }

  async function _applySnippetRuleSelection(snippetId: string, nextRuleIds: string[]) {
    const next = new Set(uniqueStrings(nextRuleIds))
    const current = new Set(
      rules.value.filter((r) => r.snippetIds?.includes(snippetId)).map((r) => r.id),
    )
    await Promise.all(
      rules.value.map(async (rule) => {
        const has = current.has(rule.id)
        const shouldHave = next.has(rule.id)
        if (has === shouldHave) return
        const updatedIds = shouldHave
          ? uniqueStrings([...(rule.snippetIds ?? []), snippetId])
          : (rule.snippetIds ?? []).filter((id) => id !== snippetId)
        await ruleApi.update(rule.id, { ...rule, snippetIds: updatedIds })
      }),
    )
  }

  async function _applyRuleSnippetSelection(ruleId: string, nextSnippetIds: string[]) {
    const next = new Set(uniqueStrings(nextSnippetIds))
    const current = new Set(
      snippets.value.filter((s) => s.ruleIds?.includes(ruleId)).map((s) => s.id),
    )
    await Promise.all(
      snippets.value.map(async (snippet) => {
        const has = current.has(snippet.id)
        const shouldHave = next.has(snippet.id)
        if (has === shouldHave) return
        const updatedIds = shouldHave
          ? uniqueStrings([...(snippet.ruleIds ?? []), ruleId])
          : (snippet.ruleIds ?? []).filter((id) => id !== ruleId)
        await snippetApi.update(snippet.id, { ...snippet, ruleIds: updatedIds })
      }),
    )
  }

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
      originalSnippet.value = null
      editSnippetRuleIds.value = []
      originalSnippetRuleIds.value = []
      snippetDirty.value = false
      return
    }
    const found = snippets.value.find((s) => s.id === selectedSnippetId.value)
    editSnippet.value = found ? cloneValue(found) : null
    originalSnippet.value = found ? cloneValue(found) : null
    editSnippetRuleIds.value = rules.value
      .filter((r) => r.snippetIds?.includes(selectedSnippetId.value!))
      .map((r) => r.id)
    originalSnippetRuleIds.value = cloneValue(editSnippetRuleIds.value)
    refreshDirty()
  }

  function _syncEditRule() {
    if (!selectedRuleId.value) {
      editRule.value = null
      originalRule.value = null
      editRuleSnippetIds.value = []
      originalRuleSnippetIds.value = []
      ruleDirty.value = false
      return
    }
    const found = rules.value.find((r) => r.id === selectedRuleId.value)
    editRule.value = found ? cloneValue(found) : null
    originalRule.value = found ? cloneValue(found) : null
    editRuleSnippetIds.value = snippets.value
      .filter((s) => s.ruleIds?.includes(selectedRuleId.value!))
      .map((s) => s.id)
    originalRuleSnippetIds.value = cloneValue(editRuleSnippetIds.value)
    refreshDirty()
  }

  watch(selectedSnippetId, _syncEditSnippet)
  watch(selectedRuleId, _syncEditRule)

  async function addSnippet(snippet: CodeSnippet, ruleIds: string[]): Promise<string | null> {
    if (!snippet.code.trim()) {
      Toast.error('代码内容不能为空')
      return null
    }
    const nextRuleIds = uniqueStrings(ruleIds)
    saving.value = true
    try {
      const res = await snippetApi.add({ ...snippet, ruleIds: nextRuleIds })
      const id = res.data.id
      if (nextRuleIds.length) await _applySnippetRuleSelection(id, nextRuleIds)
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
    const err = _validateRule(rule)
    if (err) {
      Toast.error(err)
      return null
    }
    const nextSnippetIds = uniqueStrings(snippetIds)
    const nextRule = {
      ...rule,
      snippetIds: nextSnippetIds,
    }
    saving.value = true
    try {
      const res = await ruleApi.add(nextRule)
      const id = res.data.id
      if (nextSnippetIds.length) await _applyRuleSnippetSelection(id, nextSnippetIds)
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

  async function saveSnippet() {
    if (!editSnippet.value?.code.trim()) {
      Toast.error('代码内容不能为空')
      return
    }
    const nextRuleIds = uniqueStrings(editSnippetRuleIds.value)
    saving.value = true
    try {
      await snippetApi.update(editSnippet.value.id, { ...editSnippet.value, ruleIds: nextRuleIds })
      await _applySnippetRuleSelection(editSnippet.value.id, nextRuleIds)
      await fetchAll()
      snippetDirty.value = false
      Toast.success('保存成功')
    } catch {
      Toast.error('保存失败')
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
    const nextSnippetIds = uniqueStrings(editRuleSnippetIds.value)
    const nextRule = {
      ...editRule.value,
      snippetIds: nextSnippetIds,
    }
    saving.value = true
    try {
      await ruleApi.update(editRule.value.id, nextRule)
      await _applyRuleSnippetSelection(editRule.value.id, nextSnippetIds)
      await fetchAll()
      ruleDirty.value = false
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
      ? ids.filter((n) => n !== snippetId)
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
      refreshDirty()
      return
    }
    editSnippet.value = {
      ...editSnippet.value,
      [field]: cloneValue(originalSnippet.value[field]),
    }
    refreshDirty()
  }

  function revertRuleField(field: keyof InjectionRule | 'snippetIds') {
    if (!originalRule.value || !editRule.value) return
    if (field === 'snippetIds') {
      editRuleSnippetIds.value = cloneValue(originalRuleSnippetIds.value)
      refreshDirty()
      return
    }
    editRule.value = {
      ...editRule.value,
      [field]: cloneValue(originalRule.value[field]),
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

  function confirmDeleteSnippet() {
    if (!editSnippet.value) return
    const id = editSnippet.value.id
    Dialog.warning({
      title: '删除代码片段',
      description: `确认删除代码片段 ${id}? 删除后无法恢复`,
      confirmType: 'danger',
      async onConfirm() {
        try {
          await _applySnippetRuleSelection(id, [])
          await snippetApi.delete(id)
          setSnippetsItems(snippetsResp.value.items.filter((s) => s.id !== id))
          if (selectedSnippetId.value === id) selectedSnippetId.value = null
          editSnippet.value = null
          editSnippetRuleIds.value = []
          snippetDirty.value = false
          Toast.success('代码片段已删除')
        } catch {
          Toast.error('删除失败')
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
          await _applyRuleSnippetSelection(id, [])
          await ruleApi.delete(id)
          setRulesItems(rulesResp.value.items.filter((r) => r.id !== id))
          if (selectedRuleId.value === id) selectedRuleId.value = null
          editRule.value = null
          editRuleSnippetIds.value = []
          ruleDirty.value = false
          Toast.success('规则已删除')
        } catch {
          Toast.error('删除失败')
        }
      },
    })
  }

  async function batchSetSnippetEnabled(ids: string[], enabled: boolean) {
    const targets = uniqueStrings(ids)
    if (!targets.length) return
    saving.value = true
    try {
      await Promise.all(
        targets.map(async (id) => {
          const snippet = snippets.value.find((s) => s.id === id)
          if (!snippet) return
          await snippetApi.update(id, { ...snippet, enabled })
        }),
      )
      await fetchAll()
      Toast.success(enabled ? '批量启用成功' : '批量禁用成功')
    } catch {
      Toast.error('批量状态更新失败')
    } finally {
      saving.value = false
    }
  }

  async function batchSetRuleEnabled(ids: string[], enabled: boolean) {
    const targets = uniqueStrings(ids)
    if (!targets.length) return
    saving.value = true
    try {
      await Promise.all(
        targets.map(async (id) => {
          const rule = rules.value.find((r) => r.id === id)
          if (!rule) return
          await ruleApi.update(id, { ...rule, enabled })
        }),
      )
      await fetchAll()
      Toast.success(enabled ? '批量启用成功' : '批量禁用成功')
    } catch {
      Toast.error('批量状态更新失败')
    } finally {
      saving.value = false
    }
  }

  async function batchDeleteSnippets(ids: string[]) {
    const targets = uniqueStrings(ids)
    if (!targets.length) return
    saving.value = true
    try {
      const deletedIds: string[] = []
      await Promise.all(
        targets.map(async (id) => {
          await _applySnippetRuleSelection(id, [])
          await snippetApi.delete(id)
          deletedIds.push(id)
        }),
      )
      setSnippetsItems(snippetsResp.value.items.filter((s) => !deletedIds.includes(s.id)))
      setRulesItems(
        rulesResp.value.items.map((rule) => ({
          ...rule,
          snippetIds: (rule.snippetIds ?? []).filter((id) => !deletedIds.includes(id)),
        })),
      )
      if (selectedSnippetId.value && deletedIds.includes(selectedSnippetId.value)) {
        selectedSnippetId.value = null
      }
      _syncEditSnippet()
      _syncEditRule()
      refreshDirty()
      Toast.success('批量删除成功')
    } catch {
      Toast.error('批量删除失败')
    } finally {
      saving.value = false
    }
  }

  async function batchDeleteRules(ids: string[]) {
    const targets = uniqueStrings(ids)
    if (!targets.length) return
    saving.value = true
    try {
      const deletedIds: string[] = []
      await Promise.all(
        targets.map(async (id) => {
          await _applyRuleSnippetSelection(id, [])
          await ruleApi.delete(id)
          deletedIds.push(id)
        }),
      )
      setRulesItems(rulesResp.value.items.filter((r) => !deletedIds.includes(r.id)))
      setSnippetsItems(
        snippetsResp.value.items.map((snippet) => ({
          ...snippet,
          ruleIds: (snippet.ruleIds ?? []).filter((id) => !deletedIds.includes(id)),
        })),
      )
      if (selectedRuleId.value && deletedIds.includes(selectedRuleId.value)) {
        selectedRuleId.value = null
      }
      _syncEditRule()
      _syncEditSnippet()
      refreshDirty()
      Toast.success('批量删除成功')
    } catch {
      Toast.error('批量删除失败')
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
    refreshDirty,
    rulesUsingSnippet,
    snippetsInRule,
    fetchAll,
    addSnippet,
    saveSnippet,
    toggleSnippetEnabled,
    confirmDeleteSnippet,
    toggleRuleInSnippetEditor,
    updateEditSnippet,
    revertSnippetField,
    revertSnippetAll,
    isSnippetFieldDirty,
    addRule,
    saveRule,
    toggleRuleEnabled,
    confirmDeleteRule,
    toggleSnippetInRuleEditor,
    updateEditRule,
    revertRuleField,
    revertRuleAll,
    isRuleFieldDirty,
    batchSetSnippetEnabled,
    batchSetRuleEnabled,
    batchDeleteSnippets,
    batchDeleteRules,
  }
}

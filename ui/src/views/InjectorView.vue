<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ruleApi, snippetApi } from '@/apis'
import { type ActiveTab, type CodeSnippet, type InjectionRule, type ItemList } from '@/types'
import {
  Dialog,
  IconPlug,
  Toast,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
} from '@halo-dev/components'

import SnippetEditor from './components/SnippetEditor.vue'
import RuleEditor from './components/RuleEditor.vue'
import RelationPanel from './components/RelationPanel.vue'
import SnippetFormModal from './components/SnippetFormModal.vue'
import RuleFormModal from './components/RuleFormModal.vue'
import { rulePreview } from '@/views/composables/util.ts'

const activeTab = ref<ActiveTab>('snippets')
const loading = ref(false)
const saving = ref(false)

const snippetsResp = ref<ItemList<CodeSnippet>>(emptyList())
const rulesResp = ref<ItemList<InjectionRule>>(emptyList())

const snippets = computed(() => snippetsResp.value.items)
const rules = computed(() => rulesResp.value.items)

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

const selectedSnippetId = ref<string | null>(null)
const selectedRuleId = ref<string | null>(null)

const editSnippet = ref<CodeSnippet | null>(null)
const editRule = ref<InjectionRule | null>(null)
const editDirty = ref(false)
const editSnippetRuleIds = ref<string[]>([])

const rulesUsingSnippet = computed(() => {
  if (!selectedSnippetId.value) return []
  return rules.value.filter((r) => r.snippetIds?.includes(selectedSnippetId.value!))
})

const snippetsInRule = computed(() => {
  if (!selectedRuleId.value) return []
  const rule = rules.value.find((r) => r.id === selectedRuleId.value)
  if (!rule?.snippetIds?.length) return []
  return rule.snippetIds
    .map((n) => snippets.value.find((s) => s.id === n))
    .filter((s): s is CodeSnippet => !!s)
})

const showSnippetModal = ref(false)
const showRuleModal = ref(false)

async function fetchAll() {
  loading.value = true
  try {
    const [sr, rr] = await Promise.all([snippetApi.list(), ruleApi.list()])
    snippetsResp.value = sr.data
    rulesResp.value = rr.data
    loadSelectedSnippet()
    loadSelectedRule()
  } catch {
    Toast.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function loadSelectedSnippet() {
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

function loadSelectedRule() {
  if (!selectedRuleId.value) {
    editRule.value = null
    editDirty.value = false
    return
  }
  const found = rules.value.find((r) => r.id === selectedRuleId.value)
  editRule.value = found ? JSON.parse(JSON.stringify(found)) : null
  editDirty.value = false
}

onMounted(fetchAll)
watch(selectedSnippetId, loadSelectedSnippet)
watch(selectedRuleId, loadSelectedRule)

async function handleAddSnippet(snippet: CodeSnippet, ruleIds: string[]) {
  if (!snippet.code.trim()) {
    Toast.error('代码内容不能为空')
    return
  }
  saving.value = true
  try {
    const res = await snippetApi.add(snippet)
    if (ruleIds.length) await applySnippetRuleSelection(res.data.id, ruleIds)
    showSnippetModal.value = false
    await fetchAll()
    selectedSnippetId.value = res.data.id
    Toast.success('代码块已创建')
  } catch {
    Toast.error('创建失败')
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
    await applySnippetRuleSelection(editSnippet.value.id, editSnippetRuleIds.value)
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

async function confirmDeleteSnippet() {
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

async function applySnippetRuleSelection(snippetId: string, nextRuleIds: string[]) {
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

function toggleRuleInSnippetEditor(ruleId: string) {
  const ids = editSnippetRuleIds.value
  editSnippetRuleIds.value = ids.includes(ruleId)
    ? ids.filter((id) => id !== ruleId)
    : [...ids, ruleId]
  editDirty.value = true
}

async function handleAddRule(rule: InjectionRule) {
  const pats = uniqueStrings(rule.pathPatterns.map((p) => p.pathPattern))
  if (!pats.length) {
    Toast.error('至少需要一条路径规则')
    return
  }
  if ((rule.mode === 'SELECTOR' || rule.mode === 'ID') && !rule.match.trim()) {
    Toast.error('请填写匹配内容')
    return
  }
  rule.pathPatterns = pats.map((p) => ({ pathPattern: p }))
  saving.value = true
  try {
    const res = await ruleApi.add(rule)
    showRuleModal.value = false
    await fetchAll()
    selectedRuleId.value = res.data.id
    Toast.success('规则已创建')
  } catch {
    Toast.error('创建失败')
  } finally {
    saving.value = false
  }
}

async function saveRule() {
  if (!editRule.value) return
  const pats = uniqueStrings(editRule.value.pathPatterns.map((p) => p.pathPattern))
  if (!pats.length) {
    Toast.error('至少需要一条路径规则')
    return
  }
  if (
    (editRule.value.mode === 'SELECTOR' || editRule.value.mode === 'ID') &&
    !editRule.value.match.trim()
  ) {
    Toast.error('请填写匹配内容')
    return
  }
  editRule.value.pathPatterns = pats.map((p) => ({ pathPattern: p }))
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

async function confirmDeleteRule() {
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

function jumpToRule(id: string) {
  activeTab.value = 'rules'
  selectedRuleId.value = id
}

function jumpToSnippet(id: string) {
  activeTab.value = 'snippets'
  selectedSnippetId.value = id
}

function uniqueStrings(values: string[]) {
  const seen = new Set<string>()
  return values.map((v) => v.trim()).filter((v) => v && !seen.has(v) && seen.add(v))
}
</script>

<template>
  <div id="injector-view">
    <SnippetFormModal
      v-if="showSnippetModal"
      :rules="rules"
      :saving="saving"
      @close="showSnippetModal = false"
      @submit="handleAddSnippet"
    />
    <RuleFormModal
      v-if="showRuleModal"
      :saving="saving"
      :snippets="snippets"
      @close="showRuleModal = false"
      @submit="handleAddRule"
    />

    <VPageHeader title="Injector">
      <template #icon>
        <IconPlug />
      </template>
    </VPageHeader>

    <div class=":uno: m-0 md:m-4">
      <VCard :body-class="['injector-view-card-body']" style="height: calc(100vh - 5.5rem)">
        <div class=":uno: h-full flex divide-x divide-gray-100">
          <div class=":uno: aside h-full flex-none flex flex-col overflow-hidden">
            <div
              class=":uno: sticky top-0 z-10 h-12 flex items-center gap-4 border-b bg-white px-4 shrink-0"
            >
              <button
                v-for="tab in [
                  { key: 'snippets', label: '代码块', count: snippets.length },
                  { key: 'rules', label: '注入规则', count: rules.length },
                ]"
                :key="tab.key"
                :class="
                  activeTab === tab.key
                    ? ':uno: text-primary'
                    : ':uno: text-gray-500 hover:text-gray-800'
                "
                class=":uno: text-sm font-medium transition-colors whitespace-nowrap"
                @click="activeTab = tab.key as ActiveTab"
              >
                {{ tab.label }}
                <span class=":uno: ml-0.5 text-xs">({{ tab.count }})</span>
              </button>
            </div>

            <VLoading v-if="loading" />
            <ul v-else class=":uno: flex-1 overflow-y-auto divide-y divide-gray-100">
              <template v-if="activeTab === 'snippets'">
                <li
                  v-if="!snippets.length"
                  class=":uno: flex flex-col items-center justify-center gap-3 py-10 px-4"
                >
                  <span class=":uno: text-sm text-gray-500">暂无代码块</span>
                  <VButton size="sm" type="secondary" @click="showSnippetModal = true">
                    创建第一个
                  </VButton>
                </li>
                <li
                  v-for="s in snippets"
                  :key="s.id"
                  class=":uno: relative cursor-pointer"
                  @click="selectedSnippetId = s.id"
                >
                  <div
                    v-show="selectedSnippetId === s.id"
                    class=":uno: bg-secondary absolute inset-y-0 left-0 w-0.5"
                  />
                  <div class=":uno: flex flex-col px-4 py-2.5 gap-1 hover:bg-gray-50">
                    <div class=":uno: flex items-center justify-between gap-2">
                      <span class=":uno: flex-1 min-w-0 text-sm text-gray-900 font-medium truncate">
                        {{ s.name || s.id }}
                      </span>
                      <span
                        :class="s.enabled ? ':uno: bg-primary' : ':uno: bg-gray-500'"
                        :title="s.enabled ? '已启用' : '已停用'"
                        class=":uno: shrink-0 w-1.5 h-1.5 rounded-full"
                      />
                    </div>
                    <p v-if="s.description" class=":uno: text-xs text-gray-500 line-clamp-1">
                      {{ s.description }}
                    </p>
                  </div>
                </li>
              </template>

              <template v-else>
                <li
                  v-if="!rules.length"
                  class=":uno: flex flex-col items-center justify-center gap-3 py-10 px-4"
                >
                  <span class=":uno: text-sm text-gray-500">暂无注入规则</span>
                  <VButton size="sm" type="secondary" @click="showRuleModal = true">
                    创建第一个
                  </VButton>
                </li>
                <li
                  v-for="r in rules"
                  :key="r.id"
                  class=":uno: relative cursor-pointer"
                  @click="selectedRuleId = r.id"
                >
                  <div
                    v-show="selectedRuleId === r.id"
                    class=":uno: bg-secondary absolute inset-y-0 left-0 w-0.5"
                  />
                  <div class=":uno: flex flex-col px-4 py-2.5 gap-1.5 hover:bg-gray-50">
                    <div class=":uno: flex items-center justify-between gap-2">
                      <span class=":uno: flex-1 min-w-0 text-sm text-gray-900 font-medium truncate">
                        {{ r.name || r.id }}
                      </span>
                      <span
                        :class="r.enabled ? ':uno: bg-primary' : ':uno: bg-gray-500'"
                        :title="r.enabled ? '已启用' : '已停用'"
                        class=":uno: shrink-0 w-1.5 h-1.5 rounded-full"
                      />
                    </div>
                    <div class=":uno: flex flex-wrap gap-1">
                      <span class=":uno: text-xs text-gray-500 py-0.5 rounded">
                        {{ rulePreview(r) }}
                      </span>
                    </div>
                    <p v-if="r.description" class=":uno: text-xs text-gray-500 mb-1.5 line-clamp-1">
                      {{ r.description }}
                    </p>
                    <div class=":uno: flex flex-wrap gap-1">
                      <span
                        v-for="p in r.pathPatterns ?? []"
                        :key="p.pathPattern"
                        class=":uno: text-xs text-gray-500 px-1 py-0.5 mr-1 mt-1 rounded border"
                      >
                        {{ p.pathPattern }}
                      </span>
                    </div>
                  </div>
                </li>
              </template>
            </ul>

            <div class=":uno: h-12 flex items-center justify-center border-t bg-white shrink-0">
              <VButton
                size="sm"
                type="secondary"
                @click="
                  activeTab === 'snippets' ? (showSnippetModal = true) : (showRuleModal = true)
                "
              >
                {{ activeTab === 'snippets' ? '新建代码块' : '新建规则' }}
              </VButton>
            </div>
          </div>

          <div class=":uno: main h-full flex-none flex flex-col overflow-hidden">
            <SnippetEditor
              v-if="activeTab === 'snippets'"
              :dirty="editDirty"
              :rules="rules"
              :saving="saving"
              :selected-rule-ids="editSnippetRuleIds"
              :snippet="editSnippet"
              @delete="confirmDeleteSnippet"
              @save="saveSnippet"
              @toggle-enabled="toggleSnippetEnabled"
              @toggle-rule="toggleRuleInSnippetEditor"
              @field-change="editDirty = true"
              @update:snippet="editSnippet = $event"
            />
            <RuleEditor
              v-else
              :dirty="editDirty"
              :rule="editRule"
              :saving="saving"
              :snippets="snippets"
              @delete="confirmDeleteRule"
              @save="saveRule"
              @toggle-enabled="toggleRuleEnabled"
              @toggle-snippet="toggleSnippetInRuleEditor"
              @field-change="editDirty = true"
              @update:rule="editRule = $event"
            />
          </div>

          <div class=":uno: aside h-full flex-none flex flex-col overflow-hidden">
            <RelationPanel
              :mode="activeTab"
              :rules-using-snippet="rulesUsingSnippet"
              :selected-rule-id="selectedRuleId"
              :selected-snippet-id="selectedSnippetId"
              :snippets-in-rule="snippetsInRule"
              @jump-to-rule="jumpToRule"
              @jump-to-snippet="jumpToSnippet"
            />
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

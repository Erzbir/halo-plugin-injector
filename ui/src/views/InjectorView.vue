<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Dialog, VButton, VCard, VPageHeader } from '@halo-dev/components'

import type { ActiveTab } from '@/types'
import { useInjectorData } from './composables/useInjectorData.ts'
import type { BatchActionResult } from './composables/useInjectorData.ts'
import { rulePreview } from './composables/util.ts'

import SnippetEditor from './components/SnippetEditor.vue'
import RuleEditor from './components/RuleEditor.vue'
import RelationPanel from './components/RelationPanel.vue'
import SnippetFormModal from './components/SnippetFormModal.vue'
import RuleFormModal from './components/RuleFormModal.vue'
import InjectorTopTabs from './components/InjectorTopTabs.vue'
import InjectorSidebar from './components/InjectorSidebar.vue'
import PluginIcon from '@/components/PluginIcon.vue'

const activeTab = ref<ActiveTab>('snippets')
type SortField = 'name' | 'createdAt'
type SortOrder = 'asc' | 'desc'
type SortMode = 'name-asc' | 'name-desc' | 'createdAt-asc' | 'createdAt-desc'

const snippetSortField = ref<SortField>('createdAt')
const snippetSortOrder = ref<SortOrder>('desc')
const ruleSortField = ref<SortField>('createdAt')
const ruleSortOrder = ref<SortOrder>('desc')
const sortModeOptions: Array<{ value: SortMode; label: string }> = [
  { value: 'name-asc', label: '按名称升序' },
  { value: 'name-desc', label: '按名称降序' },
  { value: 'createdAt-asc', label: '按创建时间升序' },
  { value: 'createdAt-desc', label: '按创建时间降序' },
]

const showSnippetModal = ref(false)
const showRuleModal = ref(false)
const batchMode = ref(false)
const batchSelectedIds = ref<string[]>([])
const relationDrawerOpen = ref(false)
const mobileEditorOpen = ref(false)
const topTabs: Array<{ key: ActiveTab; label: string }> = [
  { key: 'snippets', label: '代码片段' },
  { key: 'rules', label: '注入规则' },
]

const {
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
} = useInjectorData()

const editDirty = computed(() =>
  activeTab.value === 'snippets' ? snippetDirty.value : ruleDirty.value,
)
const activeRelationCount = computed(() =>
  activeTab.value === 'snippets' ? rulesUsingSnippet.value.length : snippetsInRule.value.length,
)

const snippetDirtyFields = computed(() => ({
  name: isSnippetFieldDirty('name'),
  description: isSnippetFieldDirty('description'),
  code: isSnippetFieldDirty('code'),
  ruleIds: isSnippetFieldDirty('ruleIds'),
}))

const ruleDirtyFields = computed(() => ({
  name: isRuleFieldDirty('name'),
  description: isRuleFieldDirty('description'),
  mode: isRuleFieldDirty('mode'),
  match: isRuleFieldDirty('match'),
  position: isRuleFieldDirty('position'),
  matchRule: isRuleFieldDirty('matchRule'),
  snippetIds: isRuleFieldDirty('snippetIds'),
}))

const sortedSnippets = computed(() =>
  sortItems(snippets.value, snippetSortField.value, snippetSortOrder.value),
)
const sortedRules = computed(() => sortItems(rules.value, ruleSortField.value, ruleSortOrder.value))
const activeSortMode = computed<SortMode>({
  get: () => {
    const field = activeTab.value === 'snippets' ? snippetSortField.value : ruleSortField.value
    const order = activeTab.value === 'snippets' ? snippetSortOrder.value : ruleSortOrder.value
    return `${field}-${order}` as SortMode
  },
  set: (mode) => {
    const [field, order] = mode.split('-') as [SortField, SortOrder]
    if (activeTab.value === 'snippets') {
      snippetSortField.value = field
      snippetSortOrder.value = order
      return
    }
    ruleSortField.value = field
    ruleSortOrder.value = order
  },
})
const currentItems = computed(() =>
  activeTab.value === 'snippets' ? sortedSnippets.value : sortedRules.value,
)
const allBatchSelected = computed(
  () =>
    currentItems.value.length > 0 && batchSelectedIds.value.length === currentItems.value.length,
)

function itemName(item: { id: string; name?: string }) {
  const name = item.name?.trim()
  return name || item.id
}

function itemCreatedAt(item: { metadata?: { creationTimestamp?: string | null } }) {
  const timestamp = item.metadata?.creationTimestamp
  if (!timestamp) return 0
  const parsed = Date.parse(timestamp)
  return Number.isNaN(parsed) ? 0 : parsed
}

function sortItems<
  T extends { id: string; name?: string; metadata?: { creationTimestamp?: string | null } },
>(items: T[], field: SortField, order: SortOrder) {
  const list = [...items]
  list.sort((a, b) => {
    const compared =
      field === 'name'
        ? itemName(a).localeCompare(itemName(b), 'zh-CN')
        : itemCreatedAt(a) - itemCreatedAt(b)
    return order === 'asc' ? compared : -compared
  })
  return list
}

async function confirmDiscardChanges() {
  if (!editDirty.value) return true
  return await new Promise<boolean>((resolve) => {
    Dialog.warning({
      title: '放弃修改',
      description: '存在未保存的修改, 确认放弃当前修改吗?',
      confirmText: '放弃修改',
      cancelText: '继续编辑',
      confirmType: 'danger',
      onConfirm() {
        resolve(true)
      },
      onCancel() {
        resolve(false)
      },
    })
  })
}

async function guardWithDiscard(action: () => void) {
  const shouldContinue = await confirmDiscardChanges()
  if (!shouldContinue) return
  if (activeTab.value === 'snippets') revertSnippetAll()
  else revertRuleAll()
  action()
}

async function toggleBatchMode() {
  if (batchMode.value) {
    batchMode.value = false
    batchSelectedIds.value = []
    return
  }
  await guardWithDiscard(() => {
    batchMode.value = true
    batchSelectedIds.value = []
    mobileEditorOpen.value = false
    relationDrawerOpen.value = false
  })
}

function toggleBatchSelect(id: string) {
  const set = new Set(batchSelectedIds.value)
  if (set.has(id)) set.delete(id)
  else set.add(id)
  batchSelectedIds.value = [...set]
}

function toggleSelectAll() {
  if (allBatchSelected.value) {
    batchSelectedIds.value = []
    return
  }
  batchSelectedIds.value = currentItems.value.map((item) => item.id)
}

async function batchEnableSelected() {
  if (!batchSelectedIds.value.length) return
  const result =
    activeTab.value === 'snippets'
      ? await batchSetSnippetEnabled(batchSelectedIds.value, true)
      : await batchSetRuleEnabled(batchSelectedIds.value, true)
  applyBatchResult(result)
}

async function batchDisableSelected() {
  if (!batchSelectedIds.value.length) return
  const result =
    activeTab.value === 'snippets'
      ? await batchSetSnippetEnabled(batchSelectedIds.value, false)
      : await batchSetRuleEnabled(batchSelectedIds.value, false)
  applyBatchResult(result)
}

function applyBatchResult(result: BatchActionResult) {
  batchSelectedIds.value = result.failedIds
}

async function batchDeleteSelected() {
  if (!batchSelectedIds.value.length) return
  const count = batchSelectedIds.value.length
  const kind = activeTab.value === 'snippets' ? '代码片段' : '规则'
  await new Promise<void>((resolve) => {
    Dialog.warning({
      title: `批量删除${kind}`,
      description: `确认删除选中的 ${count} 个${kind}吗? 删除后无法恢复.`,
      confirmType: 'danger',
      confirmText: '确认删除',
      cancelText: '取消',
      async onConfirm() {
        const result =
          activeTab.value === 'snippets'
            ? await batchDeleteSnippets(batchSelectedIds.value)
            : await batchDeleteRules(batchSelectedIds.value)
        applyBatchResult(result)
        resolve()
      },
      onCancel() {
        resolve()
      },
    })
  })
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (!editDirty.value) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => {
  fetchAll()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

async function handleAddSnippet(...args: Parameters<typeof addSnippet>) {
  const id = await addSnippet(...args)
  if (id) showSnippetModal.value = false
}

async function handleAddRule(...args: Parameters<typeof addRule>) {
  const id = await addRule(...args)
  if (id) showRuleModal.value = false
}

async function jumpToRule(id: string) {
  await guardWithDiscard(() => {
    activeTab.value = 'rules'
    selectedRuleId.value = id
    mobileEditorOpen.value = true
    relationDrawerOpen.value = false
  })
}

async function jumpToSnippet(id: string) {
  await guardWithDiscard(() => {
    activeTab.value = 'snippets'
    selectedSnippetId.value = id
    mobileEditorOpen.value = true
    relationDrawerOpen.value = false
  })
}

async function handleSwitchTab(tab: ActiveTab) {
  if (activeTab.value === tab) return
  if (batchMode.value) {
    activeTab.value = tab
    batchSelectedIds.value = []
    mobileEditorOpen.value = false
    relationDrawerOpen.value = false
    return
  }
  await guardWithDiscard(() => {
    activeTab.value = tab
    mobileEditorOpen.value = false
    relationDrawerOpen.value = false
  })
}

async function handleSelectSnippet(id: string) {
  if (selectedSnippetId.value === id) return
  await guardWithDiscard(() => {
    selectedSnippetId.value = id
    mobileEditorOpen.value = true
  })
}

async function handleSelectRule(id: string) {
  if (selectedRuleId.value === id) return
  await guardWithDiscard(() => {
    selectedRuleId.value = id
    mobileEditorOpen.value = true
  })
}
</script>

<template>
  <div id="injector-view">
    <SnippetFormModal
      v-if="showSnippetModal"
      :rules="sortedRules"
      :saving="saving"
      @close="showSnippetModal = false"
      @submit="handleAddSnippet"
    />
    <RuleFormModal
      v-if="showRuleModal"
      :saving="saving"
      :snippets="sortedSnippets"
      @close="showRuleModal = false"
      @submit="handleAddRule"
    />

    <VPageHeader title="Injector">
      <template #icon><PluginIcon /></template>
    </VPageHeader>

    <div class=":uno: m-0 md:m-4">
      <VCard :body-class="['injector-view-card-body']" class="injector-view-card">
        <div class=":uno: h-full flex flex-col">
          <InjectorTopTabs :active-tab="activeTab" :tabs="topTabs" @switch="handleSwitchTab" />
          <div class=":uno: flex-1 min-h-0 overflow-hidden">
            <div
              class="injector-workspace"
              :class="{ 'show-mobile-editor': mobileEditorOpen }"
            >
              <InjectorSidebar
                :active-tab="activeTab"
                :active-sort-mode="activeSortMode"
                :all-batch-selected="allBatchSelected"
                :batch-mode="batchMode"
                :batch-selected-ids="batchSelectedIds"
                :loading="loading"
                :rule-preview="rulePreview"
                :rules="sortedRules"
                :saving="saving"
                :selected-rule-id="selectedRuleId"
                :selected-snippet-id="selectedSnippetId"
                :snippets="sortedSnippets"
                :sort-mode-options="sortModeOptions"
                @batch-delete="batchDeleteSelected"
                @batch-disable="batchDisableSelected"
                @batch-enable="batchEnableSelected"
                @open-create="
                  activeTab === 'snippets' ? (showSnippetModal = true) : (showRuleModal = true)
                "
                @select-rule="handleSelectRule"
                @select-snippet="handleSelectSnippet"
                @toggle-batch-mode="toggleBatchMode"
                @toggle-batch-select="toggleBatchSelect"
                @toggle-select-all="toggleSelectAll"
                @update:sort-mode="activeSortMode = $event as SortMode"
              />

              <div class=":uno: main h-full min-w-0 flex flex-col overflow-hidden">
                <div class="mobile-editor-navigation">
                  <VButton size="sm" @click="mobileEditorOpen = false">返回列表</VButton>
                </div>
                <div v-if="batchMode" class=":uno: h-full flex flex-col">
                  <div
                    class=":uno: sticky top-0 z-10 min-h-12 flex items-center border-b bg-white px-4 py-2 shrink-0"
                  >
                    <h2 class=":uno: text-gray-900 font-semibold text-sm">批量操作</h2>
                  </div>
                </div>
                <SnippetEditor
                  v-else-if="activeTab === 'snippets'"
                  :dirty="editDirty"
                  :rules="sortedRules"
                  :saving="saving"
                  :selected-rule-ids="editSnippetRuleIds"
                  :snippet="editSnippet"
                  :dirty-fields="snippetDirtyFields"
                  :relation-count="activeRelationCount"
                  @delete="confirmDeleteSnippet"
                  @save="saveSnippet"
                  @field-change="() => undefined"
                  @revert-field="revertSnippetField"
                  @revert-all="revertSnippetAll"
                  @toggle-enabled="toggleSnippetEnabled"
                  @toggle-rule="toggleRuleInSnippetEditor"
                  @update:snippet="updateEditSnippet"
                  @open-relations="relationDrawerOpen = true"
                />
                <RuleEditor
                  v-else
                  :dirty="editDirty"
                  :rule="editRule"
                  :saving="saving"
                  :selected-snippet-ids="editRuleSnippetIds"
                  :snippets="sortedSnippets"
                  :dirty-fields="ruleDirtyFields"
                  :relation-count="activeRelationCount"
                  @delete="confirmDeleteRule"
                  @save="saveRule"
                  @field-change="() => undefined"
                  @revert-field="revertRuleField"
                  @revert-all="revertRuleAll"
                  @toggle-enabled="toggleRuleEnabled"
                  @toggle-snippet="toggleSnippetInRuleEditor"
                  @update:rule="updateEditRule"
                  @open-relations="relationDrawerOpen = true"
                />
              </div>

              <button
                v-if="relationDrawerOpen"
                aria-label="关闭关联信息"
                class="relation-drawer-backdrop"
                type="button"
                @click="relationDrawerOpen = false"
              />
              <aside
                v-if="!batchMode && relationDrawerOpen"
                aria-label="关联信息"
                class="relation-drawer"
              >
                <div class=":uno: h-12 flex items-center justify-between border-b px-4 shrink-0">
                  <h2 class=":uno: text-sm font-semibold text-gray-900">关联信息</h2>
                  <VButton size="sm" @click="relationDrawerOpen = false">关闭</VButton>
                </div>
                <div class=":uno: min-h-0 flex-1">
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
              </aside>
            </div>
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

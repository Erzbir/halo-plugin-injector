<script lang="ts" setup>
import { computed, getCurrentInstance, onBeforeUnmount, onMounted, ref } from 'vue'
import { Dialog, IconAddCircle, VButton, VCard, VPageHeader } from '@halo-dev/components'

import { MODE_OPTIONS, type ActiveTab, type InjectionRule } from '@/types'
import { useInjectorData } from './composables/useInjectorData.ts'
import type { BatchActionResult } from './composables/useInjectorData.ts'
import { rulePreview } from './composables/util.ts'

import SnippetEditor from './components/SnippetEditor.vue'
import RuleEditor from './components/RuleEditor.vue'
import RelationPanel from './components/RelationPanel.vue'
import SnippetFormModal from './components/SnippetFormModal.vue'
import RuleFormModal from './components/RuleFormModal.vue'
import InjectorTopTabs from './components/InjectorTopTabs.vue'
import InjectorListToolbar from './components/InjectorListToolbar.vue'
import InjectorSidebar from './components/InjectorSidebar.vue'
import BatchOperationPanel from './components/BatchOperationPanel.vue'
import PluginIcon from '@/components/PluginIcon.vue'

const activeTab = ref<ActiveTab>('snippets')
type SortField = 'name' | 'createdAt'
type SortOrder = 'asc' | 'desc'
type SortMode = 'name-asc' | 'name-desc' | 'createdAt-asc' | 'createdAt-desc'

const snippetSortField = ref<SortField>('createdAt')
const snippetSortOrder = ref<SortOrder>('desc')
const ruleSortField = ref<SortField>('createdAt')
const ruleSortOrder = ref<SortOrder>('desc')
const snippetSearchQuery = ref('')
const ruleSearchQuery = ref('')
const snippetStatusFilter = ref('all')
const ruleStatusFilter = ref('all')
const ruleModeFilter = ref('all')
const statusFilterOptions = [
  { value: 'all', label: '全部' },
  { value: 'enabled', label: '已启用' },
  { value: 'disabled', label: '已停用' },
]
const modeFilterOptions = [
  { value: 'all', label: '全部' },
  ...MODE_OPTIONS.map((option) => ({ value: option.value, label: option.label })),
]
const sortModeOptions: Array<{ value: SortMode; label: string }> = [
  { value: 'name-asc', label: '名称升序' },
  { value: 'name-desc', label: '名称降序' },
  { value: 'createdAt-asc', label: '创建时间升序' },
  { value: 'createdAt-desc', label: '创建时间降序' },
]

const showSnippetModal = ref(false)
const showRuleModal = ref(false)
const batchMode = ref(false)
const batchSelectedIds = ref<string[]>([])
const lastBatchResult = ref<(BatchActionResult & { action: string }) | null>(null)
const relationDrawerOpen = ref(false)
const mobileEditorOpen = ref(false)
const topTabs: Array<{ key: ActiveTab; label: string }> = [
  { key: 'snippets', label: '代码片段' },
  { key: 'rules', label: '注入规则' },
]

const {
  loading,
  loadingMoreSnippets,
  loadingMoreRules,
  snippetLoadError,
  ruleLoadError,
  saving,
  snippets,
  rules,
  snippetsTotal,
  rulesTotal,
  hasMoreSnippets,
  hasMoreRules,
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
  loadMoreSnippets,
  loadMoreRules,
  addSnippet,
  saveSnippet,
  setSnippetEnabled,
  confirmDeleteSnippet,
  toggleRuleInSnippetEditor,
  updateEditSnippet,
  revertSnippetField,
  revertSnippetAll,
  isSnippetFieldDirty,
  addRule,
  saveRule,
  setRuleEnabled,
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
const filteredSnippets = computed(() =>
  sortedSnippets.value.filter(
    (snippet) =>
      matchesQuery(snippet, snippetSearchQuery.value) &&
      matchesStatus(snippet.enabled, snippetStatusFilter.value),
  ),
)
const filteredRules = computed(() =>
  sortedRules.value.filter(
    (rule) =>
      matchesQuery(rule, ruleSearchQuery.value) &&
      matchesStatus(rule.enabled, ruleStatusFilter.value) &&
      (ruleModeFilter.value === 'all' || rule.mode === ruleModeFilter.value),
  ),
)
const activeSearchQuery = computed({
  get: () => (activeTab.value === 'snippets' ? snippetSearchQuery.value : ruleSearchQuery.value),
  set: (query: string) => {
    if (activeTab.value === 'snippets') snippetSearchQuery.value = query
    else ruleSearchQuery.value = query
  },
})
const activeStatusFilter = computed({
  get: () => (activeTab.value === 'snippets' ? snippetStatusFilter.value : ruleStatusFilter.value),
  set: (status: string) => {
    if (activeTab.value === 'snippets') snippetStatusFilter.value = status
    else ruleStatusFilter.value = status
  },
})
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
  activeTab.value === 'snippets' ? filteredSnippets.value : filteredRules.value,
)
const selectedBatchItems = computed(() => {
  const selected = new Set(batchSelectedIds.value)
  const items = activeTab.value === 'snippets' ? sortedSnippets.value : sortedRules.value
  return items.filter((item) => selected.has(item.id))
})
const allBatchSelected = computed(
  () =>
    currentItems.value.length > 0 &&
    currentItems.value.every((item) => batchSelectedIds.value.includes(item.id)),
)
const activeLoadedCount = computed(() =>
  activeTab.value === 'snippets' ? snippets.value.length : rules.value.length,
)
const activeTotal = computed(() =>
  activeTab.value === 'snippets' ? snippetsTotal.value : rulesTotal.value,
)
const activeHasMore = computed(() =>
  activeTab.value === 'snippets' ? hasMoreSnippets.value : hasMoreRules.value,
)
const activeLoadingMore = computed(() =>
  activeTab.value === 'snippets' ? loadingMoreSnippets.value : loadingMoreRules.value,
)
const activeLoadError = computed(() =>
  activeTab.value === 'snippets' ? snippetLoadError.value : ruleLoadError.value,
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

function matchesQuery(item: { id: string; name?: string; description?: string }, query: string) {
  const keyword = query.trim().toLocaleLowerCase()
  if (!keyword) return true
  return [item.name, item.id, item.description]
    .filter(Boolean)
    .some((value) => value!.toLocaleLowerCase().includes(keyword))
}

function matchesStatus(enabled: boolean, filter: string) {
  if (filter === 'enabled') return enabled
  if (filter === 'disabled') return !enabled
  return true
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

async function guardWithDiscard(action: () => void | Promise<void>) {
  const shouldContinue = await confirmDiscardChanges()
  if (!shouldContinue) return
  if (activeTab.value === 'snippets') revertSnippetAll()
  else revertRuleAll()
  await action()
}

async function toggleBatchMode() {
  if (batchMode.value) {
    batchMode.value = false
    batchSelectedIds.value = []
    lastBatchResult.value = null
    return
  }
  await guardWithDiscard(() => {
    batchMode.value = true
    batchSelectedIds.value = []
    lastBatchResult.value = null
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
  const visibleIds = new Set(currentItems.value.map((item) => item.id))
  if (allBatchSelected.value) {
    batchSelectedIds.value = batchSelectedIds.value.filter((id) => !visibleIds.has(id))
    return
  }
  batchSelectedIds.value = [...new Set([...batchSelectedIds.value, ...visibleIds])]
}

async function loadMoreActiveItems() {
  if (activeTab.value === 'snippets') await loadMoreSnippets()
  else await loadMoreRules()
}

async function refreshAll() {
  await guardWithDiscard(fetchAll)
}

async function batchEnableSelected() {
  if (!batchSelectedIds.value.length) return
  const result =
    activeTab.value === 'snippets'
      ? await batchSetSnippetEnabled(batchSelectedIds.value, true)
      : await batchSetRuleEnabled(batchSelectedIds.value, true)
  applyBatchResult(result, '批量启用')
}

async function batchDisableSelected() {
  if (!batchSelectedIds.value.length) return
  const result =
    activeTab.value === 'snippets'
      ? await batchSetSnippetEnabled(batchSelectedIds.value, false)
      : await batchSetRuleEnabled(batchSelectedIds.value, false)
  applyBatchResult(result, '批量停用')
}

function applyBatchResult(result: BatchActionResult, action: string) {
  batchSelectedIds.value = result.failedIds
  lastBatchResult.value = { ...result, action }
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
        applyBatchResult(result, '批量删除')
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

type AppRouter = {
  beforeEach: (guard: () => Promise<boolean>) => () => void
}

const appRouter = (getCurrentInstance()?.proxy as { $router?: AppRouter } | null)?.$router
let removeRouteGuard: (() => void) | undefined

onMounted(() => {
  fetchAll()
  window.addEventListener('beforeunload', handleBeforeUnload)
  removeRouteGuard = appRouter?.beforeEach(async () => await confirmDiscardChanges())
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  removeRouteGuard?.()
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
    lastBatchResult.value = null
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

async function requestSnippetEnabled(enabled: boolean) {
  if (!snippetDirty.value) {
    await setSnippetEnabled(enabled)
    return
  }
  confirmSaveBeforeStatusChange(enabled, '代码片段', saveSnippet, setSnippetEnabled)
}

async function requestRuleEnabled(enabled: boolean) {
  if (!ruleDirty.value) {
    await setRuleEnabled(enabled)
    return
  }
  confirmSaveBeforeStatusChange(enabled, '规则', saveRule, setRuleEnabled)
}

function confirmSaveBeforeStatusChange(
  enabled: boolean,
  kind: string,
  save: () => Promise<boolean>,
  updateStatus: (enabled: boolean) => Promise<void>,
) {
  const action = enabled ? '启用' : '停用'
  Dialog.warning({
    title: `保存并${action}${kind}`,
    description: `当前${kind}存在未保存的修改, 需要先保存修改再${action}`,
    confirmText: `保存并${action}`,
    cancelText: '继续编辑',
    async onConfirm() {
      const saved = await save()
      if (saved) await updateStatus(enabled)
    },
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
      <template #actions>
        <VButton size="sm" @click="toggleBatchMode">
          {{ batchMode ? '退出批量操作' : '批量操作' }}
        </VButton>
        <VButton
          :disabled="batchMode"
          size="md"
          type="secondary"
          @click="activeTab === 'snippets' ? (showSnippetModal = true) : (showRuleModal = true)"
        >
          <template #icon><IconAddCircle /></template>
          新建
        </VButton>
      </template>
    </VPageHeader>

    <div class=":uno: m-0 md:m-4">
      <VCard :body-class="['injector-view-card-body']" class="injector-view-card">
        <template #header>
          <div class=":uno: w-full min-w-0">
            <InjectorTopTabs :active-tab="activeTab" :tabs="topTabs" @switch="handleSwitchTab" />
            <InjectorListToolbar
              :active-tab="activeTab"
              :active-sort-mode="activeSortMode"
              :loading="loading"
              :mode-filter="ruleModeFilter"
              :mode-filter-options="modeFilterOptions"
              :search-query="activeSearchQuery"
              :sort-mode-options="sortModeOptions"
              :status-filter="activeStatusFilter"
              :status-filter-options="statusFilterOptions"
              @refresh="refreshAll"
              @update:mode-filter="ruleModeFilter = $event as InjectionRule['mode'] | 'all'"
              @update:search-query="activeSearchQuery = $event"
              @update:sort-mode="activeSortMode = $event as SortMode"
              @update:status-filter="activeStatusFilter = $event"
            />
          </div>
        </template>
        <div class=":uno: h-full flex flex-col">
          <div class=":uno: flex-1 min-h-0 overflow-hidden">
            <div class="injector-workspace" :class="{ 'show-mobile-editor': mobileEditorOpen }">
              <InjectorSidebar
                :active-tab="activeTab"
                :active-sort-mode="activeSortMode"
                :all-batch-selected="allBatchSelected"
                :batch-mode="batchMode"
                :batch-selected-ids="batchSelectedIds"
                :has-more="activeHasMore"
                :loading="loading"
                :loading-more="activeLoadingMore"
                :load-error="activeLoadError"
                :loaded-count="activeLoadedCount"
                :rule-preview="rulePreview"
                :rules="filteredRules"
                :saving="saving"
                :selected-rule-id="selectedRuleId"
                :selected-snippet-id="selectedSnippetId"
                :snippets="filteredSnippets"
                :total="activeTotal"
                @batch-delete="batchDeleteSelected"
                @batch-disable="batchDisableSelected"
                @batch-enable="batchEnableSelected"
                @select-rule="handleSelectRule"
                @select-snippet="handleSelectSnippet"
                @load-more="loadMoreActiveItems"
                @retry="fetchAll"
                @toggle-batch-select="toggleBatchSelect"
                @toggle-select-all="toggleSelectAll"
              />

              <div class=":uno: main h-full min-w-0 flex flex-col overflow-hidden">
                <div class="mobile-editor-navigation">
                  <VButton size="sm" @click="mobileEditorOpen = false">返回列表</VButton>
                </div>
                <BatchOperationPanel
                  v-if="batchMode"
                  :active-tab="activeTab"
                  :items="selectedBatchItems"
                  :result="lastBatchResult"
                  :saving="saving"
                  @clear="batchSelectedIds = []"
                  @delete="batchDeleteSelected"
                  @disable="batchDisableSelected"
                  @enable="batchEnableSelected"
                  @remove="toggleBatchSelect"
                />
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
                  @set-enabled="requestSnippetEnabled"
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
                  @set-enabled="requestRuleEnabled"
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

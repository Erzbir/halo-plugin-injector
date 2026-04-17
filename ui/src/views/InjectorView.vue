<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Dialog, VButton, VCard, VLoading, VPageHeader } from '@halo-dev/components'

import type { ActiveTab } from '@/types'
import { useInjectorData } from './composables/useInjectorData.ts'
import { rulePreview } from './composables/util.ts'

import ItemListV from './components/ItemListV.vue'
import SnippetEditor from './components/SnippetEditor.vue'
import RuleEditor from './components/RuleEditor.vue'
import RelationPanel from './components/RelationPanel.vue'
import SnippetFormModal from './components/SnippetFormModal.vue'
import RuleFormModal from './components/RuleFormModal.vue'
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
  editDirty,
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
const activeSortModeLabel = computed(
  () => sortModeOptions.find((o) => o.value === activeSortMode.value)?.label ?? '按创建时间降序',
)
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
  if (activeTab.value === 'snippets') await batchSetSnippetEnabled(batchSelectedIds.value, true)
  else await batchSetRuleEnabled(batchSelectedIds.value, true)
}

async function batchDisableSelected() {
  if (!batchSelectedIds.value.length) return
  if (activeTab.value === 'snippets') await batchSetSnippetEnabled(batchSelectedIds.value, false)
  else await batchSetRuleEnabled(batchSelectedIds.value, false)
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
        if (activeTab.value === 'snippets') await batchDeleteSnippets(batchSelectedIds.value)
        else await batchDeleteRules(batchSelectedIds.value)
        batchSelectedIds.value = []
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
  })
}

async function jumpToSnippet(id: string) {
  await guardWithDiscard(() => {
    activeTab.value = 'snippets'
    selectedSnippetId.value = id
  })
}

async function handleSwitchTab(tab: ActiveTab) {
  if (activeTab.value === tab) return
  if (batchMode.value) {
    activeTab.value = tab
    batchSelectedIds.value = []
    return
  }
  await guardWithDiscard(() => {
    activeTab.value = tab
  })
}

async function handleSelectSnippet(id: string) {
  if (selectedSnippetId.value === id) return
  await guardWithDiscard(() => {
    selectedSnippetId.value = id
  })
}

async function handleSelectRule(id: string) {
  if (selectedRuleId.value === id) return
  await guardWithDiscard(() => {
    selectedRuleId.value = id
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
      <VCard :body-class="['injector-view-card-body']" style="height: calc(100vh - 5.5rem)">
        <div class=":uno: h-full flex flex-col">
          <div class=":uno: h-12 shrink-0 border-b bg-gray-100 px-4">
            <div class=":uno: h-full flex items-center gap-2">
              <div
                v-for="tab in [
                  { key: 'snippets', label: '代码片段', count: snippets.length },
                  { key: 'rules', label: '注入规则', count: rules.length },
                ]"
                :key="tab.key"
                :class="
                  activeTab === tab.key ? ':uno: bg-white text-gray-900' : ':uno: text-gray-500'
                "
                class=":uno: group relative h-9 min-w-24 px-3 flex items-center justify-center text-sm font-medium transition-colors whitespace-nowrap cursor-pointer select-none overflow-hidden"
                @click="handleSwitchTab(tab.key as ActiveTab)"
              >
                <span
                  :class="activeTab === tab.key ? ':uno: opacity-0' : ':uno: opacity-0'"
                  class=":uno: absolute inset-1 bg-gray-900/10 pointer-events-none transition-opacity"
                />
                <span class=":uno: relative z-1 text-center">{{ tab.label }}</span>
              </div>
            </div>
          </div>
          <div class=":uno: flex-1 overflow-x-auto">
            <div class=":uno: h-full min-w-[980px] flex divide-x divide-gray-100">
              <div class=":uno: aside aside-left h-full flex-none flex flex-col overflow-hidden">
                <div
                  class=":uno: sticky top-0 z-10 h-12 flex items-center justify-between gap-2 border-b bg-white px-4 shrink-0"
                >
                  <div class=":uno: flex items-center gap-4 text-sm font-semibold text-gray-900">
                    {{
                      activeTab === 'snippets'
                        ? `代码片段列表 (${snippets.length})`
                        : `注入规则列表 (${rules.length})`
                    }}
                  </div>
                  <div class=":uno: flex items-center gap-1.5">
                    <label
                      class=":uno: relative h-6 inline-flex items-center rounded-md border border-gray-200 bg-white px-1.5 text-[10px] text-gray-700 hover:border-gray-300"
                    >
                      <span class=":uno: pointer-events-none text-[10px] whitespace-nowrap">
                        {{ activeSortModeLabel }}
                      </span>
                      <select
                        v-model="activeSortMode"
                        class=":uno: sort-select-native absolute inset-0 opacity-0 cursor-pointer text-[10px]"
                      >
                        <option
                          v-for="option in sortModeOptions"
                          :key="option.value"
                          :value="option.value"
                        >
                          {{ option.label }}
                        </option>
                      </select>
                    </label>
                  </div>
                </div>

                <VLoading v-if="loading" />

                <div
                  v-if="batchMode"
                  class=":uno: h-10 shrink-0 flex items-center justify-between gap-1 border-b bg-gray-50 px-2"
                >
                  <VButton size="xs" @click="toggleSelectAll">
                    {{ allBatchSelected ? '取消全选' : '全选' }}
                  </VButton>
                  <div class=":uno: flex items-center gap-1">
                    <VButton
                      :disabled="!batchSelectedIds.length || saving"
                      size="xs"
                      @click="batchEnableSelected"
                    >
                      启用
                    </VButton>
                    <VButton
                      :disabled="!batchSelectedIds.length || saving"
                      size="xs"
                      @click="batchDisableSelected"
                    >
                      禁用
                    </VButton>
                    <VButton
                      :disabled="!batchSelectedIds.length || saving"
                      size="xs"
                      type="danger"
                      @click="batchDeleteSelected"
                    >
                      删除
                    </VButton>
                  </div>
                </div>

                <div class=":uno: flex-1 overflow-y-auto max-h-[calc(100vh-17rem)]">
                  <ItemListV
                    v-if="activeTab === 'snippets'"
                    :batch-mode="batchMode"
                    :batch-selected-ids="batchSelectedIds"
                    :items="sortedSnippets"
                    :selected-id="selectedSnippetId"
                    empty-text="暂无代码片段"
                    @create="showSnippetModal = true"
                    @select="handleSelectSnippet"
                    @toggle-batch-select="toggleBatchSelect"
                  />

                  <ItemListV
                    v-else
                    :batch-mode="batchMode"
                    :batch-selected-ids="batchSelectedIds"
                    :items="sortedRules"
                    :selected-id="selectedRuleId"
                    :stretch="true"
                    empty-text="暂无注入规则"
                    @create="showRuleModal = true"
                    @select="handleSelectRule"
                    @toggle-batch-select="toggleBatchSelect"
                  >
                    <template #meta="{ item: r }">
                      <span class=":uno: text-xs text-gray-500">{{ rulePreview(r) }}</span>
                    </template>
                  </ItemListV>
                </div>

                <div
                  class=":uno: h-12 flex items-center justify-center gap-2 border-t bg-white shrink-0"
                >
                  <VButton v-if="!batchMode" size="sm" @click="toggleBatchMode">批量操作</VButton>
                  <VButton v-else size="sm" @click="toggleBatchMode">退出批量操作</VButton>
                  <VButton
                    :disabled="batchMode"
                    size="sm"
                    type="secondary"
                    @click="
                      activeTab === 'snippets' ? (showSnippetModal = true) : (showRuleModal = true)
                    "
                  >
                    {{ activeTab === 'snippets' ? '新建代码片段' : '新建规则' }}
                  </VButton>
                </div>
              </div>

              <div class=":uno: main h-full flex-none flex flex-col overflow-hidden">
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
                  @delete="confirmDeleteSnippet"
                  @save="saveSnippet"
                  @field-change="() => undefined"
                  @revert-field="revertSnippetField"
                  @revert-all="revertSnippetAll"
                  @toggle-enabled="toggleSnippetEnabled"
                  @toggle-rule="toggleRuleInSnippetEditor"
                  @update:snippet="updateEditSnippet"
                />
                <RuleEditor
                  v-else
                  :dirty="editDirty"
                  :rule="editRule"
                  :saving="saving"
                  :selected-snippet-ids="editRuleSnippetIds"
                  :snippets="sortedSnippets"
                  :dirty-fields="ruleDirtyFields"
                  @delete="confirmDeleteRule"
                  @save="saveRule"
                  @field-change="() => undefined"
                  @revert-field="revertRuleField"
                  @revert-all="revertRuleAll"
                  @toggle-enabled="toggleRuleEnabled"
                  @toggle-snippet="toggleSnippetInRuleEditor"
                  @update:rule="updateEditRule"
                />
              </div>

              <div
                v-if="!batchMode"
                class=":uno: aside aside-right h-full flex-none flex flex-col overflow-hidden"
              >
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
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

<style scoped>
.sort-select-native,
.sort-select-native option {
  font-size: 11px;
}
</style>

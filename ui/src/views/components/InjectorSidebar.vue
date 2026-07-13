<script lang="ts" setup>
import { VButton, VLoading } from '@halo-dev/components'
import type { ActiveTab } from '@/types'
import ItemListV from './ItemListV.vue'
import SelectDropdown from './SelectDropdown.vue'

defineProps<{
  activeTab: ActiveTab
  loading: boolean
  saving: boolean
  batchMode: boolean
  batchSelectedIds: string[]
  allBatchSelected: boolean
  sortModeOptions: Array<{ value: string; label: string }>
  activeSortMode: string
  searchQuery: string
  statusFilter: string
  modeFilter: string
  statusFilterOptions: Array<{ value: string; label: string }>
  modeFilterOptions: Array<{ value: string; label: string }>
  snippets: Array<{ id: string; name: string; description?: string; enabled: boolean }>
  rules: Array<{ id: string; name: string; description?: string; enabled: boolean }>
  loadedCount: number
  total: number
  hasMore: boolean
  loadingMore: boolean
  selectedSnippetId?: string | null
  selectedRuleId?: string | null
  rulePreview: (rule: unknown) => string
}>()

const emit = defineEmits<{
  (e: 'update:sort-mode', mode: string): void
  (e: 'update:search-query', query: string): void
  (e: 'update:status-filter', status: string): void
  (e: 'update:mode-filter', mode: string): void
  (e: 'load-more'): void
  (e: 'toggle-batch-mode'): void
  (e: 'open-create'): void
  (e: 'toggle-select-all'): void
  (e: 'batch-enable'): void
  (e: 'batch-disable'): void
  (e: 'batch-delete'): void
  (e: 'toggle-batch-select', id: string): void
  (e: 'select-snippet', id: string): void
  (e: 'select-rule', id: string): void
}>()
</script>

<template>
  <div class=":uno: aside aside-left h-full flex-none flex flex-col overflow-hidden">
    <div class=":uno: sticky top-0 z-10 border-b bg-white px-3 py-2 space-y-2 shrink-0">
      <input
        :value="searchQuery"
        aria-label="搜索列表"
        class=":uno: w-full rounded-md border border-gray-200 px-2.5 py-1.5 text-xs focus:border-primary focus:outline-none"
        placeholder="搜索名称、ID 或描述"
        type="search"
        @input="emit('update:search-query', ($event.target as HTMLInputElement).value)"
      />
      <div class=":uno: flex items-center justify-between gap-1.5 min-w-0">
        <div class=":uno: min-w-0 shrink">
          <SelectDropdown
            :model-value="activeSortMode"
            :options="sortModeOptions"
            :full-width="false"
            size="sm"
            align="end"
            placeholder="排序"
            @update:model-value="emit('update:sort-mode', $event)"
          />
        </div>
        <VButton
          v-if="!batchMode"
          size="sm"
          class=":uno: shrink-0"
          @click="emit('toggle-batch-mode')"
          >批量操作</VButton
        >
        <VButton v-else size="sm" class=":uno: shrink-0" @click="emit('toggle-batch-mode')"
          >退出批量操作</VButton
        >
        <VButton
          :disabled="batchMode"
          size="sm"
          type="secondary"
          class=":uno: shrink-0"
          @click="emit('open-create')"
          >新建</VButton
        >
      </div>
    </div>

    <VLoading v-if="loading" />

    <div
      v-if="batchMode"
      class=":uno: h-10 shrink-0 flex items-center justify-between gap-1 border-b bg-gray-50 px-2"
    >
      <VButton size="xs" @click="emit('toggle-select-all')">
        {{ allBatchSelected ? '取消全选' : '全选' }}
      </VButton>
      <div class=":uno: flex items-center gap-1">
        <VButton
          :disabled="!batchSelectedIds.length || saving"
          size="xs"
          @click="emit('batch-enable')"
        >
          启用
        </VButton>
        <VButton
          :disabled="!batchSelectedIds.length || saving"
          size="xs"
          @click="emit('batch-disable')"
        >
          禁用
        </VButton>
        <VButton
          :disabled="!batchSelectedIds.length || saving"
          size="xs"
          type="danger"
          @click="emit('batch-delete')"
        >
          删除
        </VButton>
      </div>
      <div class=":uno: grid grid-cols-2 gap-1.5">
        <SelectDropdown
          :model-value="statusFilter"
          :options="statusFilterOptions"
          size="xs"
          @update:model-value="emit('update:status-filter', $event)"
        />
        <SelectDropdown
          v-if="activeTab === 'rules'"
          :model-value="modeFilter"
          :options="modeFilterOptions"
          size="xs"
          @update:model-value="emit('update:mode-filter', $event)"
        />
        <span v-else />
      </div>
    </div>

    <div class=":uno: flex-1 overflow-y-auto">
      <ItemListV
        v-if="activeTab === 'snippets'"
        :batch-mode="batchMode"
        :batch-selected-ids="batchSelectedIds"
        :items="snippets"
        :selected-id="selectedSnippetId"
        empty-text="暂无代码片段"
        @select="emit('select-snippet', $event)"
        @toggle-batch-select="emit('toggle-batch-select', $event)"
      />

      <ItemListV
        v-else
        :batch-mode="batchMode"
        :batch-selected-ids="batchSelectedIds"
        :items="rules"
        :selected-id="selectedRuleId"
        :stretch="true"
        empty-text="暂无注入规则"
        @select="emit('select-rule', $event)"
        @toggle-batch-select="emit('toggle-batch-select', $event)"
      >
        <template #meta="{ item: rule }">
          <span class=":uno: text-xs text-gray-500">{{ rulePreview(rule) }}</span>
        </template>
      </ItemListV>
    </div>

    <div class=":uno: h-9 flex items-center justify-between gap-2 border-t bg-white px-4 shrink-0">
      <span class=":uno: text-xs text-gray-500 whitespace-nowrap">
        显示 {{ activeTab === 'snippets' ? snippets.length : rules.length }} / {{ total }} 个
      </span>
      <VButton v-if="hasMore" :disabled="loadingMore" size="xs" @click="emit('load-more')">
        {{ loadingMore ? '加载中...' : `继续加载 (${loadedCount}/${total})` }}
      </VButton>
    </div>
  </div>
</template>

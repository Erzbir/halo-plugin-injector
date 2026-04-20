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
  snippets: Array<{ id: string; name: string; description?: string; enabled: boolean }>
  rules: Array<{ id: string; name: string; description?: string; enabled: boolean }>
  selectedSnippetId?: string | null
  selectedRuleId?: string | null
  rulePreview: (rule: unknown) => string
}>()

const emit = defineEmits<{
  (e: 'update:sort-mode', mode: string): void
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
    <div class=":uno: sticky top-0 z-10 h-12 flex items-center justify-end gap-2 border-b bg-white px-4 shrink-0">
      <div class=":uno: flex items-center gap-1.5">
        <SelectDropdown
          :model-value="activeSortMode"
          :options="sortModeOptions"
          :full-width="false"
          size="sm"
          align="end"
          placeholder="排序"
          @update:model-value="emit('update:sort-mode', $event)"
        />
        <VButton v-if="!batchMode" size="sm" @click="emit('toggle-batch-mode')">批量操作</VButton>
        <VButton v-else size="sm" @click="emit('toggle-batch-mode')">退出批量操作</VButton>
        <VButton :disabled="batchMode" size="sm" type="secondary" @click="emit('open-create')">新建</VButton>
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
        <VButton :disabled="!batchSelectedIds.length || saving" size="xs" @click="emit('batch-enable')">
          启用
        </VButton>
        <VButton :disabled="!batchSelectedIds.length || saving" size="xs" @click="emit('batch-disable')">
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
        {{ activeTab === 'snippets' ? `共 ${snippets.length} 个` : `共 ${rules.length} 个` }}
      </span>
    </div>
  </div>
</template>

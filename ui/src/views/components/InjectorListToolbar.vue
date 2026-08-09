<script lang="ts" setup>
import { IconRefreshLine } from '@halo-dev/components'
import type { ActiveTab } from '@/types'
import SelectDropdown from './SelectDropdown.vue'

defineProps<{
  activeTab: ActiveTab
  activeSortMode: string
  loading: boolean
  modeFilter: string
  modeFilterOptions: Array<{ value: string; label: string }>
  searchQuery: string
  sortModeOptions: Array<{ value: string; label: string }>
  statusFilter: string
  statusFilterOptions: Array<{ value: string; label: string }>
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
  (e: 'update:mode-filter', mode: string): void
  (e: 'update:search-query', query: string): void
  (e: 'update:sort-mode', mode: string): void
  (e: 'update:status-filter', status: string): void
}>()
</script>

<template>
  <div class=":uno: block w-full bg-gray-50 px-4 py-3">
    <div
      class=":uno: relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
    >
      <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
        <div
          class=":uno: bg-white inline-flex items-center w-full relative box-border border border-gray-300 h-9 rounded-base overflow-hidden focus-within:border-primary focus-within:shadow-sm sm:w-auto sm:max-w-lg transition-all"
        >
          <input
            :value="searchQuery"
            placeholder="输入关键词搜索"
            aria-label="搜索当前列表"
            class=":uno: formkit-input resize-none w-full text-black block transition-all px-3 text-sm"
            type="search"
            @input="emit('update:search-query', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>

      <div class=":uno: flex flex-wrap items-center gap-1.5 sm:gap-5">
        <SelectDropdown
          :model-value="statusFilter"
          :options="statusFilterOptions"
          appearance="plain"
          aria-label="按状态筛选"
          prefix="状态"
          @update:model-value="emit('update:status-filter', $event)"
        />
        <SelectDropdown
          v-if="activeTab === 'rules'"
          :model-value="modeFilter"
          :options="modeFilterOptions"
          appearance="plain"
          aria-label="按注入模式筛选"
          prefix="模式"
          @update:model-value="emit('update:mode-filter', $event)"
        />
        <SelectDropdown
          :model-value="activeSortMode"
          :options="sortModeOptions"
          align="end"
          appearance="plain"
          aria-label="列表排序"
          prefix="排序"
          @update:model-value="emit('update:sort-mode', $event)"
        />
        <button
          :disabled="loading"
          aria-label="刷新列表"
          class=":uno: group cursor-pointer rounded p-1 hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-60"
          title="刷新列表"
          type="button"
          @click="emit('refresh')"
        >
          <IconRefreshLine :class="loading ? ':uno: animate-spin' : ''" />
        </button>
      </div>
    </div>
  </div>
</template>

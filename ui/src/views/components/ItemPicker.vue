<script
  generic="T extends { id: string; name: string; description?: string; enabled: boolean }"
  lang="ts"
setup
>
import { computed, ref } from 'vue'
import { VButton } from '@halo-dev/components'
import StatusDot from './StatusDot.vue'

const props = defineProps<{
  items: T[]
  selectedIds: string[]
  emptyText?: string
  previewFn?: (item: T) => string
}>()

const emit = defineEmits<{
  (e: 'toggle', id: string): void
}>()

const query = ref('')
const filteredItems = computed(() => {
  const keyword = query.value.trim().toLocaleLowerCase()
  if (!keyword) return props.items
  return props.items.filter((item) => {
    const preview = props.previewFn?.(item) ?? ''
    return [item.name, item.id, item.description, preview]
      .filter(Boolean)
      .some((value) => value!.toLocaleLowerCase().includes(keyword))
  })
})
const allVisibleSelected = computed(
  () =>
    filteredItems.value.length > 0 &&
    filteredItems.value.every((item) => props.selectedIds.includes(item.id)),
)

function toggleVisibleItems() {
  const shouldSelect = !allVisibleSelected.value
  for (const item of filteredItems.value) {
    if (props.selectedIds.includes(item.id) !== shouldSelect) emit('toggle', item.id)
  }
}
</script>

<template>
  <div class=":uno: border border-gray-200 rounded-md bg-white overflow-hidden">
    <div class=":uno: flex items-center gap-2 border-b border-gray-100 p-2">
      <input
        v-model="query"
        aria-label="搜索关联项"
        class=":uno: min-w-0 flex-1 rounded-md border border-gray-200 px-2 py-1 text-xs focus:border-primary focus:outline-none"
        placeholder="搜索名称、ID 或描述"
        type="search"
      />
      <VButton :disabled="!filteredItems.length" size="xs" @click="toggleVisibleItems">
        {{ allVisibleSelected ? '取消当前结果' : '全选当前结果' }}
      </VButton>
    </div>
    <div class=":uno: max-h-56 overflow-y-auto divide-y divide-gray-100">
    <div
      v-if="!filteredItems.length"
      class=":uno: flex items-center justify-center h-14 text-xs text-gray-400"
    >
      {{ items.length ? '没有匹配的结果' : (emptyText ?? '暂无数据') }}
    </div>
    <label
      v-for="item in filteredItems"
      :key="item.id"
      :class="selectedIds.includes(item.id) ? ':uno: bg-primary/5' : ':uno: hover:bg-gray-50'"
      class=":uno: flex items-start gap-2 px-3 py-2 cursor-pointer transition-colors"
    >
      <input
        :checked="selectedIds.includes(item.id)"
        class=":uno: mt-1 shrink-0"
        type="checkbox"
        @change="emit('toggle', item.id)"
      />
      <div class=":uno: min-w-0 flex-1">
        <span class=":uno: text-sm text-gray-900 font-medium block truncate">
          {{ item.name || item.id }}
        </span>
        <span v-if="item.description" class=":uno: text-xs text-gray-500 block truncate">
          {{ item.description }}
        </span>
        <span v-if="previewFn" class=":uno: text-xs text-gray-400 block truncate font-mono mt-0.5">
          {{ previewFn(item) }}
        </span>
      </div>
      <StatusDot :enabled="item.enabled" />
    </label>
    </div>
  </div>
</template>

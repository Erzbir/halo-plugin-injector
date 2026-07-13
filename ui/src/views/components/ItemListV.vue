<script
  generic="T extends { id: string; name: string; description?: string; enabled: boolean }"
  lang="ts"
  setup
>
import StatusDot from './StatusDot.vue'

const props = defineProps<{
  items: T[]
  selectedId?: string | null
  emptyText?: string
  stretch?: boolean
  batchMode?: boolean
  batchSelectedIds?: string[]
}>()

const emit = defineEmits<{
  (e: 'select', id: string): void
  (e: 'create'): void
  (e: 'toggle-batch-select', id: string): void
}>()

function activate(id: string) {
  if (props.batchMode) emit('toggle-batch-select', id)
  else emit('select', id)
}
</script>

<template>
  <div :class="stretch ? ':uno: flex-1 overflow-y-auto' : ''">
    <slot name="placeholder" />

    <ul
      :aria-multiselectable="batchMode || undefined"
      aria-label="项目列表"
      class=":uno: divide-y divide-gray-100"
      role="listbox"
    >
      <li
        v-if="!items.length"
        class=":uno: flex flex-col items-center justify-center gap-3 py-10 px-4"
      >
        <span class=":uno: text-sm text-gray-500">{{ emptyText ?? '暂无数据' }}</span>
        <slot name="empty-action"></slot>
      </li>

      <li
        v-for="item in items"
        :key="item.id"
        :aria-selected="batchMode ? batchSelectedIds?.includes(item.id) : selectedId === item.id"
        class=":uno: relative cursor-pointer group"
        role="option"
        tabindex="0"
        @click="activate(item.id)"
        @keydown.enter.prevent="activate(item.id)"
        @keydown.space.prevent="activate(item.id)"
      >
        <div
          v-if="!batchMode && selectedId !== undefined && selectedId === item.id"
          class=":uno: bg-secondary absolute inset-y-0 left-0 w-0.5"
        />

        <div class=":uno: flex flex-col px-4 py-2.5 gap-1 hover:bg-gray-50">
          <div class=":uno: flex items-center justify-between gap-2">
            <div class=":uno: flex min-w-0 flex-1 items-center gap-2">
              <input
                v-if="batchMode"
                :checked="batchSelectedIds?.includes(item.id)"
                class=":uno: h-3.5 w-3.5 shrink-0"
                type="checkbox"
                @click.stop
                @change="emit('toggle-batch-select', item.id)"
              />
              <span class=":uno: min-w-0 flex-1 text-sm text-gray-900 font-medium truncate">
                {{ item.name || item.id }}
              </span>
            </div>
            <StatusDot :enabled="item.enabled" />
          </div>

          <p v-if="item.description" class=":uno: text-xs text-gray-500 line-clamp-1">
            {{ item.description }}
          </p>

          <slot :item="item" name="meta" />

          <slot :item="item" name="hint" />
        </div>
      </li>
    </ul>
  </div>
</template>

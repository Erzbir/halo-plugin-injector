<script
  generic="T extends { id: string; name: string; description?: string; enabled: boolean }"
  lang="ts"
  setup
>
import StatusDot from './StatusDot.vue'

defineProps<{
  items: T[]
  selectedId?: string | null
  emptyText?: string
  stretch?: boolean
}>()

const emit = defineEmits<{
  (e: 'select', id: string): void
  (e: 'create'): void
}>()
</script>

<template>
  <div :class="stretch ? ':uno: flex-1 overflow-y-auto' : ''">
    <slot name="placeholder" />

    <ul class=":uno: divide-y divide-gray-100">
      <li
        v-if="!items.length"
        class=":uno: flex flex-col items-center justify-center gap-3 py-10 px-4"
      >
        <span class=":uno: text-sm text-gray-500">{{ emptyText ?? '暂无数据' }}</span>
        <slot name="empty-action"></slot>
      </li>

      <li
        v-for="(item, index) in items"
        :key="item.id"
        class=":uno: relative cursor-pointer group"
        @click="emit('select', item.id)"
      >
        <div
          v-if="selectedId !== undefined && selectedId === item.id"
          class=":uno: bg-secondary absolute inset-y-0 left-0 w-0.5"
        />

        <div class=":uno: flex flex-col px-4 py-2.5 gap-1 hover:bg-gray-50">
          <div class=":uno: flex items-center justify-between gap-2">
            <span class=":uno: flex-1 min-w-0 text-sm text-gray-900 font-medium truncate">
              {{ item.name || item.id }}
            </span>
            <div class=":uno: flex items-center gap-1">
              <slot :index="index" :item="item" name="actions" />
              <StatusDot :enabled="item.enabled" />
            </div>
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

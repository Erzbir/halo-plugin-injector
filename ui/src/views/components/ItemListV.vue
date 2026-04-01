<script
  generic="T extends { id: string; name: string; description?: string; enabled: boolean }"
  lang="ts"
  setup
>
defineProps<{
  items: T[]
  selectedId: string | null
  emptyText?: string
}>()

const emit = defineEmits<{
  (e: 'select', id: string): void
  (e: 'create'): void
}>()
</script>

<template>
  <ul class=":uno: flex-1 overflow-y-auto divide-y divide-gray-100">
    <li
      v-if="!items.length"
      class=":uno: flex flex-col items-center justify-center gap-3 py-10 px-4"
    >
      <span class=":uno: text-sm text-gray-500">{{ emptyText ?? '暂无数据' }}</span>
      <slot name="empty-action">
        <button class=":uno: text-sm text-primary hover:underline" @click="emit('create')">
          创建第一个
        </button>
      </slot>
    </li>

    <li
      v-for="item in items"
      :key="item.id"
      class=":uno: relative cursor-pointer"
      @click="emit('select', item.id)"
    >
      <div
        v-show="selectedId === item.id"
        class=":uno: bg-secondary absolute inset-y-0 left-0 w-0.5"
      />

      <div class=":uno: flex flex-col px-4 py-2.5 gap-1 hover:bg-gray-50">
        <div class=":uno: flex items-center justify-between gap-2">
          <span class=":uno: flex-1 min-w-0 text-sm text-gray-900 font-medium truncate">
            {{ item.name || item.id }}
          </span>
          <span
            :class="item.enabled ? ':uno: bg-primary' : ':uno: bg-gray-500'"
            :title="item.enabled ? '已启用' : '已停用'"
            class=":uno: shrink-0 w-1.5 h-1.5 rounded-full"
          />
        </div>

        <p v-if="item.description" class=":uno: text-xs text-gray-500 line-clamp-1 pl-3.5">
          {{ item.description }}
        </p>

        <div class=":uno: pl-3.5">
          <slot :item="item" name="meta" />
        </div>
      </div>
    </li>
  </ul>
</template>

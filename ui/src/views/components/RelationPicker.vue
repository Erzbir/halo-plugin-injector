<script
  generic="T extends { id: string; name: string; description?: string; enabled: boolean }"
  lang="ts"
  setup
>
import ItemPicker from './ItemPicker.vue'

withDefaults(
  defineProps<{
    label: string
    items: T[]
    selectedIds: string[]
    emptyText?: string
    previewFn?: (item: T) => string
    showLabel?: boolean
  }>(),
  {
    showLabel: true,
  },
)

const emit = defineEmits<{
  (e: 'toggle', id: string): void
}>()
</script>

<template>
  <div class=":uno: space-y-1">
    <div class=":uno: flex items-center justify-between">
      <label v-if="showLabel" class=":uno: text-xs font-medium text-gray-600">{{ label }}</label>
      <span v-else />
      <span class=":uno: text-xs text-gray-400">{{ selectedIds.length }} 个已选</span>
    </div>
    <ItemPicker
      :items="items"
      :preview-fn="previewFn"
      :selected-ids="selectedIds"
      :empty-text="emptyText"
      @toggle="emit('toggle', $event)"
    />
  </div>
</template>

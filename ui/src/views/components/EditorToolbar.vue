<script lang="ts" setup>
import { VButton, VSpace } from '@halo-dev/components'

defineProps<{
  title: string
  displayId?: string
  enabled?: boolean
  showActions?: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle-enabled'): void
  (e: 'delete'): void
}>()
</script>

<template>
  <div
    class=":uno: sticky top-0 z-10 min-h-12 flex items-center justify-between border-b bg-white px-4 py-2 shrink-0"
  >
    <div
      class=":uno: group/title flex min-w-0 flex-1 items-center gap-2 overflow-hidden"
    >
      <h2 class=":uno: shrink-0 text-sm font-semibold text-gray-900">{{ title }}</h2>
      <span
        v-if="displayId"
        class=":uno: min-w-0 truncate text-xs text-gray-500"
        :title="`ID: ${displayId}`"
      >
        ID: {{ displayId }}
      </span>
    </div>
    <VSpace v-if="showActions">
      <VButton size="sm" @click="emit('toggle-enabled')">
        {{ enabled ? '禁用' : '启用' }}
      </VButton>
      <VButton size="sm" type="danger" @click="emit('delete')">删除</VButton>
    </VSpace>
  </div>
</template>

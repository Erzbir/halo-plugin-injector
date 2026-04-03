<script lang="ts" setup>
import { VButton, VSpace } from '@halo-dev/components'

defineProps<{
  title: string
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
    class=":uno: sticky top-0 z-10 h-12 flex items-center justify-between border-b bg-white px-4 shrink-0"
  >
    <h2 class=":uno: text-gray-900 font-semibold text-sm">{{ title }}</h2>
    <VSpace v-if="showActions">
      <VButton size="sm" @click="emit('toggle-enabled')">
        {{ enabled ? '禁用' : '启用' }}
      </VButton>
      <VButton size="sm" type="danger" @click="emit('delete')">删除</VButton>
    </VSpace>
  </div>
</template>

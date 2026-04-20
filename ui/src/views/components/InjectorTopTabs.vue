<script lang="ts" setup>
import { VTabbar } from '@halo-dev/components'
import type { ActiveTab } from '@/types'

defineProps<{
  activeTab: ActiveTab
  tabs: Array<{ key: ActiveTab; label: string }>
}>()

const emit = defineEmits<{
  (e: 'switch', tab: ActiveTab): void
}>()
</script>

<template>
  <div class=":uno: h-12 shrink-0 border-b bg-gray-100 px-4">
    <div class=":uno: h-full flex items-center gap-2">
      <VTabbar
        :active-id="activeTab"
        :items="tabs"
        id-key="key"
        label-key="label"
        @update:active-id="emit('switch', $event as ActiveTab)"
      />
    </div>
  </div>
</template>

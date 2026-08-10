<script lang="ts" setup>
import { VButton, VSpace, VSwitch } from '@halo-dev/components'

defineProps<{
  title: string
  displayId?: string
  enabled?: boolean
  showActions?: boolean
  dirty?: boolean
  saving?: boolean
  relationCount?: number
}>()

const emit = defineEmits<{
  (e: 'set-enabled', enabled: boolean): void
  (e: 'delete'): void
  (e: 'revert-all'): void
  (e: 'save'): void
  (e: 'open-relations'): void
}>()
</script>

<template>
  <div
    class=":uno: sticky top-0 z-10 min-h-12 flex items-center justify-between border-b bg-white px-4 py-2 shrink-0"
  >
    <div class=":uno: group/title flex min-w-0 flex-1 items-center gap-2 overflow-hidden">
      <h2 class=":uno: shrink-0 text-sm font-semibold text-gray-900">{{ title }}</h2>
      <span
        v-if="displayId"
        class=":uno: min-w-0 truncate text-xs text-gray-500"
        :title="`ID: ${displayId}`"
      >
        ID: {{ displayId }}
      </span>
      <span v-if="dirty" class=":uno: shrink-0 text-xs text-amber-600" role="status">
        有未保存修改
      </span>
    </div>
    <VSpace v-if="showActions" class=":uno: shrink-0">
      <VButton size="sm" @click="emit('open-relations')"> 关联 {{ relationCount ?? 0 }} </VButton>
      <label class=":uno: flex items-center gap-2 text-xs text-gray-600 whitespace-nowrap">
        <VSwitch
          :disabled="saving"
          :loading="saving"
          :model-value="enabled"
          @update:model-value="emit('set-enabled', $event)"
        />
      </label>
      <VButton :disabled="!dirty || saving" size="sm" @click="emit('revert-all')">撤销全部</VButton>
      <VButton :disabled="!dirty || saving" size="sm" type="primary" @click="emit('save')">
        {{ saving ? '保存中...' : '保存' }}
      </VButton>
      <VButton size="sm" type="danger" @click="emit('delete')">删除</VButton>
    </VSpace>
  </div>
</template>

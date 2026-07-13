<script lang="ts" setup>
import {
  VButton,
  vClosePopper,
  VDropdown,
  VDropdownItem,
  VSpace,
  VSwitch,
} from '@halo-dev/components'

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
    <VSpace v-if="showActions" class=":uno: shrink-0">
      <VButton size="sm" @click="emit('open-relations')">
        关联 {{ relationCount ?? 0 }}
      </VButton>
      <label class=":uno: flex items-center gap-2 text-xs text-gray-600 whitespace-nowrap">
        <VSwitch
          :disabled="saving"
          :loading="saving"
          :model-value="enabled"
          @update:model-value="emit('set-enabled', $event)"
        />
        {{ enabled ? '已启用' : '已停用' }}
      </label>
      <VButton :disabled="!dirty || saving" size="sm" @click="emit('revert-all')">撤销全部</VButton>
      <VButton :disabled="!dirty || saving" size="sm" type="primary" @click="emit('save')">
        {{ saving ? '保存中...' : '保存' }}
      </VButton>
      <VDropdown placement="bottom-end">
        <VButton size="sm">更多</VButton>
        <template #popper>
          <VDropdownItem v-close-popper type="danger" @click="emit('delete')">
            删除
          </VDropdownItem>
        </template>
      </VDropdown>
    </VSpace>
  </div>
</template>

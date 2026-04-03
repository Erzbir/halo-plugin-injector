<script lang="ts" setup>
import { VButton, VModal, VSpace } from '@halo-dev/components'

defineProps<{
  title: string
  saving: boolean
  submitLabel?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit'): void
}>()
</script>

<template>
  <VModal :title="title" :width="1000" @close="emit('close')">
    <div
      class=":uno: flex divide-x divide-gray-100 injector-editor-container"
      style="min-height: 400px"
    >
      <div class=":uno: flex-1 px-5 py-4 space-y-4 overflow-y-auto" style="width: 60%">
        <slot name="form" />
      </div>

      <div class=":uno: flex-none px-4 py-4 space-y-2 overflow-y-auto" style="width: 40%">
        <slot name="picker" />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton @click="emit('close')">取消</VButton>
        <VButton :disabled="saving" type="secondary" @click="emit('submit')">
          {{ saving ? `${submitLabel ?? '创建'}中...` : (submitLabel ?? '创建') }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<script lang="ts" setup>
import { Dialog, VButton, VModal, VSpace } from '@halo-dev/components'

const props = defineProps<{
  title: string
  saving: boolean
  dirty?: boolean
  submitLabel?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit'): void
}>()

function requestClose() {
  if (props.saving) return
  if (!props.dirty) {
    emit('close')
    return
  }
  Dialog.warning({
    title: '放弃新建内容',
    description: '当前表单存在未保存的内容, 确认关闭吗?',
    confirmText: '放弃内容',
    cancelText: '继续编辑',
    confirmType: 'danger',
    onConfirm() {
      emit('close')
    },
  })
}
</script>

<template>
  <VModal :title="title" :width="1000" @close="requestClose">
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
        <VButton :disabled="saving" @click="requestClose">取消</VButton>
        <VButton :disabled="saving" type="secondary" @click="emit('submit')">
          {{ saving ? `${submitLabel ?? '创建'}中...` : (submitLabel ?? '创建') }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

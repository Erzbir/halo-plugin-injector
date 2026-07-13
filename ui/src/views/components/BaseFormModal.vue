<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { Dialog, VButton, VModal, VSpace } from '@halo-dev/components'

const props = withDefaults(
  defineProps<{
    title: string
    saving: boolean
    dirty?: boolean
    valid?: boolean
    submitLabel?: string
  }>(),
  {
    dirty: false,
    valid: true,
  },
)

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit'): void
}>()

const modalWidth = ref(960)

function updateModalWidth() {
  modalWidth.value = Math.min(960, Math.max(280, window.innerWidth - 32))
}

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

onMounted(() => {
  updateModalWidth()
  window.addEventListener('resize', updateModalWidth)
})

onBeforeUnmount(() => window.removeEventListener('resize', updateModalWidth))
</script>

<template>
  <VModal :title="title" :width="modalWidth" @close="requestClose">
    <div class=":uno: base-form-modal-content injector-editor-container overflow-y-auto">
      <div class=":uno: base-form-modal-grid min-h-96">
        <div class=":uno: px-5 py-4 space-y-4">
          <slot name="form" />
        </div>

        <div class=":uno: base-form-modal-picker px-4 py-4 space-y-2">
          <slot name="picker" />
        </div>
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton :disabled="saving" @click="requestClose">取消</VButton>
        <VButton :disabled="saving || !valid" type="primary" @click="emit('submit')">
          {{ saving ? `${submitLabel ?? '创建'}中...` : (submitLabel ?? '创建') }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<style scoped>
.base-form-modal-content {
  max-height: calc(100dvh - 12rem);
}

.base-form-modal-grid {
  display: grid;
  grid-template-columns: minmax(0, 3fr) minmax(16rem, 2fr);
}

.base-form-modal-picker {
  border-left: 1px solid rgb(243 244 246);
}

@media (max-width: 720px) {
  .base-form-modal-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .base-form-modal-picker {
    border-top: 1px solid rgb(243 244 246);
    border-left: 0;
  }
}
</style>

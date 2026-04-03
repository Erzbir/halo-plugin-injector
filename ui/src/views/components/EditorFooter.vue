<script lang="ts" setup>
import { VButton } from '@halo-dev/components'

defineProps<{
  dirty: boolean
  saving: boolean
}>()

const emit = defineEmits<{
  (e: 'save'): void
}>()
</script>

<template>
  <div class=":uno: flex items-center justify-between pt-1 pb-2">
    <span v-if="dirty" class=":uno: text-xs text-yellow-600">有未保存的修改</span>
    <span v-else />
    <VButton :disabled="saving" type="secondary" @click="emit('save')">
      {{ saving ? '保存中...' : '保存' }}
    </VButton>
  </div>
</template>

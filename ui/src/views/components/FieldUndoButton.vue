<script lang="ts" setup>
import { computed, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    previewStartMs?: number
    resetPressMs?: number
  }>(),
  {
    previewStartMs: 1000,
    resetPressMs: 3000,
  },
)

const emit = defineEmits<{
  (e: 'undo'): void
  (e: 'reset'): void
}>()

const pressing = ref(false)
const progress = ref(0)
const suppressClick = ref(false)
const resetTriggered = ref(false)
let animationFrameId: number | null = null
let pressStartedAt = 0

function stopProgress(resetProgress = true) {
  if (animationFrameId !== null) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
  pressing.value = false
  if (resetProgress) {
    progress.value = 0
  }
}

function tickProgress() {
  if (!pressing.value) {
    return
  }
  const elapsed = performance.now() - pressStartedAt
  if (elapsed <= props.previewStartMs) {
    progress.value = 0
  } else {
    progress.value = Math.min(
      100,
      ((elapsed - props.previewStartMs) / (props.resetPressMs - props.previewStartMs)) * 100,
    )
  }
  if (!resetTriggered.value && elapsed >= props.resetPressMs) {
    resetTriggered.value = true
    suppressClick.value = true
    progress.value = 100
    emit('reset')
    return
  }
  if (progress.value < 100) {
    animationFrameId = requestAnimationFrame(tickProgress)
  }
}

function startPress() {
  stopProgress()
  suppressClick.value = false
  resetTriggered.value = false
  pressing.value = true
  pressStartedAt = performance.now()
  progress.value = 0
  animationFrameId = requestAnimationFrame(tickProgress)
}

function finishPress(triggerAction: boolean) {
  if (!pressing.value) {
    return
  }
  const elapsed = performance.now() - pressStartedAt
  suppressClick.value = true
  if (triggerAction && !resetTriggered.value) {
    if (elapsed >= props.resetPressMs) {
      emit('reset')
    } else {
      emit('undo')
    }
  }
  stopProgress()
}

function handleClick() {
  if (suppressClick.value) {
    suppressClick.value = false
    return
  }
  emit('undo')
}

const buttonStateClass = computed(() =>
  progress.value > 0
    ? ':uno: border-amber-300 text-amber-700 bg-amber-50'
    : ':uno: border-gray-200 text-gray-500',
)

const buttonText = computed(() => {
  if (!pressing.value || progress.value === 0) {
    return '撤销修改'
  }
  return '撤销全部'
})
</script>

<template>
  <button
    :class="buttonStateClass"
    class=":uno: relative overflow-hidden rounded border px-2 py-0.5 text-xs transition-colors hover:border-gray-300 hover:text-gray-700"
    title="单击或按住 1 秒内松开：撤销上一步；继续按到 3 秒：自动恢复初始值"
    type="button"
    @click="handleClick"
    @pointercancel="finishPress(false)"
    @pointerdown="startPress"
    @pointerleave="finishPress(false)"
    @pointerup="finishPress(true)"
  >
    <span
      class=":uno: pointer-events-none absolute inset-y-0 left-0 bg-amber-100/80 transition-[width]"
      :style="{ width: `${progress}%` }"
    />
    <span class=":uno: relative z-1">{{ buttonText }}</span>
  </button>
</template>

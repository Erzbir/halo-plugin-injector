<script lang="ts" setup>
import { ref } from 'vue'

const props = withDefaults(
  defineProps<{
    longPressMs?: number
  }>(),
  {
    longPressMs: 600,
  },
)

const emit = defineEmits<{
  (e: 'undo'): void
  (e: 'reset'): void
}>()

const longPressed = ref(false)
const pressing = ref(false)
const progress = ref(0)
let pressTimer: ReturnType<typeof setTimeout> | null = null
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

function clearPressTimer() {
  if (pressTimer) {
    clearTimeout(pressTimer)
    pressTimer = null
  }
}

function tickProgress() {
  if (!pressing.value) {
    return
  }
  const elapsed = performance.now() - pressStartedAt
  progress.value = Math.min(100, (elapsed / props.longPressMs) * 100)
  if (progress.value < 100) {
    animationFrameId = requestAnimationFrame(tickProgress)
  }
}

function startPress() {
  clearPressTimer()
  stopProgress()
  longPressed.value = false
  pressing.value = true
  pressStartedAt = performance.now()
  progress.value = 0
  animationFrameId = requestAnimationFrame(tickProgress)
  pressTimer = setTimeout(() => {
    longPressed.value = true
    progress.value = 100
    stopProgress(false)
    emit('reset')
  }, props.longPressMs)
}

function stopPress() {
  clearPressTimer()
  stopProgress()
}

function handleClick() {
  if (longPressed.value) {
    longPressed.value = false
    progress.value = 0
    return
  }
  emit('undo')
}
</script>

<template>
  <button
    :class="
      pressing
        ? ':uno: border-amber-300 text-amber-700 bg-amber-50'
        : ':uno: border-gray-200 text-gray-500'
    "
    class=":uno: relative overflow-hidden rounded border px-2 py-0.5 text-xs transition-colors hover:border-gray-300 hover:text-gray-700"
    title="单击撤销上一步，长按回到未修改状态"
    type="button"
    @click="handleClick"
    @pointercancel="stopPress"
    @pointerdown="startPress"
    @pointerleave="stopPress"
    @pointerup="stopPress"
  >
    <span
      class=":uno: pointer-events-none absolute inset-y-0 left-0 bg-amber-100/80 transition-[width]"
      :style="{ width: `${progress}%` }"
    />
    <span class=":uno: relative z-1">
      {{ pressing ? '长按恢复初始值' : '撤销修改' }}
    </span>
  </button>
</template>

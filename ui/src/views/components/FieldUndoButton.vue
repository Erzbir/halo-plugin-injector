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
let pressTimer: ReturnType<typeof setTimeout> | null = null

function clearPressTimer() {
  if (pressTimer) {
    clearTimeout(pressTimer)
    pressTimer = null
  }
}

function startPress() {
  clearPressTimer()
  longPressed.value = false
  pressTimer = setTimeout(() => {
    longPressed.value = true
    emit('reset')
  }, props.longPressMs)
}

function stopPress() {
  clearPressTimer()
}

function handleClick() {
  if (longPressed.value) {
    longPressed.value = false
    return
  }
  emit('undo')
}
</script>

<template>
  <button
    class=":uno: rounded border border-gray-200 px-2 py-0.5 text-xs text-gray-500 transition-colors hover:border-gray-300 hover:text-gray-700"
    title="单击撤销上一步，长按回到未修改状态"
    type="button"
    @click="handleClick"
    @pointercancel="stopPress"
    @pointerdown="startPress"
    @pointerleave="stopPress"
    @pointerup="stopPress"
  >
    撤销修改
  </button>
</template>

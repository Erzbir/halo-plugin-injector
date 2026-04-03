<script lang="ts" setup>
import { ref } from 'vue'
import { VButton } from '@halo-dev/components'
import type { PathMatchRule } from '@/types'

const props = defineProps<{
  modelValue: PathMatchRule[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: PathMatchRule[]): void
  (e: 'change'): void
}>()

const draft = ref('')

function add() {
  const value = draft.value.trim()
  if (!value) return
  if (!props.modelValue.some((p) => p.pathPattern === value)) {
    emit('update:modelValue', [...props.modelValue, { pathPattern: value }])
    emit('change')
  }
  draft.value = ''
}

function remove(pattern: string) {
  emit(
    'update:modelValue',
    props.modelValue.filter((p) => p.pathPattern !== pattern),
  )
  emit('change')
}
</script>

<template>
  <div class=":uno: space-y-2 injector-editor-container">
    <div class=":uno: flex gap-2">
      <input
        v-model="draft"
        class=":uno: flex-1 rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none"
        placeholder="输入路径规则, 如 /blog/**"
        @keydown.enter.prevent="add"
      />
      <VButton size="sm" @click="add">添加</VButton>
    </div>

    <div class=":uno: space-y-1">
      <div
        v-for="p in modelValue"
        :key="p.pathPattern"
        class=":uno: flex items-center justify-between px-3 py-1.5 bg-gray-50 rounded-md border border-gray-100"
      >
        <span class=":uno: text-xs font-mono text-gray-700 truncate">{{ p.pathPattern }}</span>
        <VButton class=":uno: ml-2 shrink-0" size="sm" type="danger" @click="remove(p.pathPattern)">
          删除
        </VButton>
      </div>
      <p v-if="!modelValue.length" class=":uno: text-xs text-gray-400 px-1">暂无路径规则</p>
    </div>
  </div>
</template>

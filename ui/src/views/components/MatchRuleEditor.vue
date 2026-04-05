<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { VButton } from '@halo-dev/components'
import type { MatchRule, MatchRuleEditorMode } from '@/types'
import { makeMatchRuleGroup } from '@/types'
import { formatMatchRule, normalizeMatchRule, parseMatchRuleDraft } from '@/views/composables/matchRule'
import MatchRuleNodeEditor from './MatchRuleNodeEditor.vue'

const props = defineProps<{
  modelValue: MatchRule
  draft?: string
  editorMode?: MatchRuleEditorMode
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: MatchRule): void
  (e: 'update:draft', value: string): void
  (e: 'update:editorMode', value: MatchRuleEditorMode): void
  (e: 'change'): void
}>()

const jsonDraft = ref(props.draft || formatMatchRule(props.modelValue))

watch(
  () => props.draft,
  (value) => {
    if (typeof value === 'string' && value !== jsonDraft.value) {
      jsonDraft.value = value
    }
  },
)

watch(
  () => props.modelValue,
  (value) => {
    if (!props.draft) {
      jsonDraft.value = formatMatchRule(value)
    }
  },
  { deep: true },
)

const currentMode = computed(() => props.editorMode ?? 'SIMPLE')
const parseError = computed(() => (parseMatchRuleDraft(jsonDraft.value) ? '' : 'JSON 格式无效'))

function switchMode(mode: MatchRuleEditorMode) {
  emit('update:editorMode', mode)
  if (mode === 'JSON') {
    const text = formatMatchRule(props.modelValue)
    jsonDraft.value = text
    emit('update:draft', text)
  }
  emit('change')
}

function updateSimple(value: MatchRule) {
  const normalized = normalizeMatchRule(value)
  const draft = formatMatchRule(normalized)
  emit('update:modelValue', normalized)
  emit('update:draft', draft)
  emit('update:editorMode', 'SIMPLE')
  emit('change')
}

function updateJsonDraft(value: string) {
  jsonDraft.value = value
  emit('update:draft', value)
  emit('update:editorMode', 'JSON')
  emit('change')
  const parsed = parseMatchRuleDraft(value)
  if (parsed) {
    emit('update:modelValue', parsed)
  }
}

function formatJson() {
  const parsed = parseMatchRuleDraft(jsonDraft.value) ?? normalizeMatchRule(props.modelValue)
  const next = formatMatchRule(parsed || makeMatchRuleGroup())
  jsonDraft.value = next
  emit('update:modelValue', parsed || makeMatchRuleGroup())
  emit('update:draft', next)
  emit('change')
}
</script>

<template>
  <div class=":uno: space-y-3">
    <div class=":uno: flex flex-wrap items-center justify-between gap-2">
      <div class=":uno: inline-flex rounded-md border border-gray-200 bg-gray-50 p-0.5">
        <button
          :class="
            currentMode === 'SIMPLE'
              ? ':uno: bg-white text-primary shadow-sm'
              : ':uno: text-gray-500'
          "
          class=":uno: rounded px-3 py-1 text-sm transition-colors"
          type="button"
          @click="switchMode('SIMPLE')"
        >
          可视化简单模式
        </button>
        <button
          :class="
            currentMode === 'JSON' ? ':uno: bg-white text-primary shadow-sm' : ':uno: text-gray-500'
          "
          class=":uno: rounded px-3 py-1 text-sm transition-colors"
          type="button"
          @click="switchMode('JSON')"
        >
          JSON 高级模式
        </button>
      </div>

      <VButton v-if="currentMode === 'JSON'" size="sm" type="secondary" @click="formatJson">
        格式化 JSON
      </VButton>
    </div>

    <MatchRuleNodeEditor
      v-if="currentMode === 'SIMPLE'"
      :model-value="normalizeMatchRule(modelValue)"
      root
      @change="emit('change')"
      @update:model-value="updateSimple"
    />

    <div v-else class=":uno: space-y-2">
      <textarea
        :value="jsonDraft"
        class=":uno: min-h-72 w-full rounded-md border border-gray-200 px-3 py-2 text-sm font-mono focus:border-primary focus:outline-none"
        spellcheck="false"
        @input="updateJsonDraft(($event.target as HTMLTextAreaElement).value)"
      />
      <p class=":uno: text-xs text-gray-500">
        这里可以直接编辑匹配规则 JSON。根节点请使用条件组，支持全部满足 / 任一满足、取反、路径匹配和模板 ID 匹配。
      </p>
      <p v-if="parseError" class=":uno: text-xs text-red-500">{{ parseError }}</p>
    </div>
  </div>
</template>

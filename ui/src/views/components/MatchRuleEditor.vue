<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { Dialog, VButton } from '@halo-dev/components'
import type { MatchRule, MatchRuleEditorMode } from '@/types'
import { makeMatchRuleGroup } from '@/types'
import {
  formatMatchRule,
  formatMatchRuleError,
  normalizeMatchRule,
  parseMatchRuleDraft,
} from '@/views/composables/matchRule'
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
const parseResult = computed(() => parseMatchRuleDraft(jsonDraft.value))
const parseError = computed(() => formatMatchRuleError(parseResult.value.error))
const jsonActionLabel = computed(() => (parseResult.value.error ? '重建 JSON' : '格式化 JSON'))
const jsonActionTitle = computed(() =>
  parseResult.value.error
    ? '当前 JSON 有误，将按当前生效规则重新生成 JSON'
    : '整理当前 JSON 的缩进与格式',
)

function switchMode(mode: MatchRuleEditorMode) {
  if (shouldConfirmModeSwitch(mode)) {
    Dialog.warning({
      title: '切换到简单模式',
      description:
        '当前 JSON 仍有错误。若继续切换到可视化简单模式，这份未保存的 JSON 内容将被覆盖。确认继续吗？',
      confirmType: 'danger',
      onConfirm() {
        applyModeSwitch(mode, true)
      },
    })
    return
  }
  applyModeSwitch(mode, false)
}

function shouldConfirmModeSwitch(mode: MatchRuleEditorMode) {
  return currentMode.value === 'JSON' && mode !== 'JSON' && !!parseResult.value.error
}

function applyModeSwitch(mode: MatchRuleEditorMode, overwriteDraft: boolean) {
  emit('update:editorMode', mode)
  if (mode === 'JSON' || overwriteDraft) {
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
  if (parseResult.value.rule) {
    emit('update:modelValue', parseResult.value.rule)
  }
}

function formatJson() {
  const parsed = parseResult.value.rule ?? normalizeMatchRule(props.modelValue)
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
          :aria-pressed="currentMode === 'SIMPLE'"
          :class="
            currentMode === 'SIMPLE'
              ? ':uno: bg-white text-primary shadow-sm'
              : ':uno: text-gray-500'
          "
          class=":uno: rounded px-3 py-1 text-sm transition-colors"
          type="button"
          @click="switchMode('SIMPLE')"
        >
          简单模式
        </button>
        <button
          :aria-pressed="currentMode === 'JSON'"
          :class="
            currentMode === 'JSON' ? ':uno: bg-white text-primary shadow-sm' : ':uno: text-gray-500'
          "
          class=":uno: rounded px-3 py-1 text-sm transition-colors"
          type="button"
          @click="switchMode('JSON')"
        >
          高级模式（JSON）
        </button>
      </div>

      <VButton
        v-if="currentMode === 'JSON'"
        :title="jsonActionTitle"
        size="sm"
        type="secondary"
        @click="formatJson"
      >
        {{ jsonActionLabel }}
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
        :class="
          parseError
            ? ':uno: border-red-300 focus:border-red-500'
            : ':uno: border-gray-200 focus:border-primary'
        "
        class=":uno: min-h-72 w-full rounded-md border px-3 py-2 text-sm font-mono focus:outline-none"
        spellcheck="false"
        @input="updateJsonDraft(($event.target as HTMLTextAreaElement).value)"
      />
      <p class=":uno: text-xs text-gray-500">
        这里可以直接编辑匹配规则 JSON。根节点请使用条件组，支持全部满足 /
        任一满足、取反、路径匹配和模板 ID 匹配。
      </p>
      <p v-if="parseError" class=":uno: text-xs text-red-500">{{ parseError }}</p>
    </div>
  </div>
</template>

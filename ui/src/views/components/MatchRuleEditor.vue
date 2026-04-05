<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { Dialog, VButton } from '@halo-dev/components'
import type { MatchRule, MatchRuleEditorMode } from '@/types'
import { makeMatchRuleGroup } from '@/types'
import {
  formatMatchRule,
  formatMatchRuleError,
  type MatchRuleValidationError,
  normalizeMatchRule,
  parseMatchRuleDraft,
  validateMatchRuleTree,
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
  (
    e: 'update:state',
    value: Partial<{
      matchRule: MatchRule
      matchRuleDraft: string
      matchRuleEditorMode: MatchRuleEditorMode
    }>,
  ): void
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
const simpleValidateResult = computed(() =>
  validateMatchRuleTree(normalizeMatchRule(props.modelValue)),
)
const simpleValidationError = computed<MatchRuleValidationError | null>(
  () => simpleValidateResult.value.error,
)
const jsonActionLabel = computed(() => (parseResult.value.error ? '重建 JSON' : '格式化 JSON'))
const jsonActionTitle = computed(() =>
  parseResult.value.error
    ? '当前 JSON 有误，将按当前生效规则重新生成 JSON'
    : '整理当前 JSON 的缩进与格式',
)

function switchMode(mode: MatchRuleEditorMode) {
  const warningConfig = getModeSwitchWarning(mode)
  if (warningConfig) {
    Dialog.warning({
      title: warningConfig.title,
      description: warningConfig.description,
      confirmType: warningConfig.confirmType,
      onConfirm() {
        applyModeSwitch(mode, warningConfig.overwriteDraft)
      },
    })
    return
  }
  applyModeSwitch(mode, false)
}

function getModeSwitchWarning(mode: MatchRuleEditorMode) {
  if (currentMode.value === 'JSON' && mode === 'SIMPLE' && parseResult.value.error) {
    return {
      title: '切换到简单模式',
      description: `已检测到当前高级模式（JSON）有错误。
如果继续切换，会回到可视化简单模式。
你当前这份未保存的 JSON 内容会被覆盖。
确认继续吗？`,
      confirmType: 'danger' as const,
      overwriteDraft: true,
    }
  }

  if (currentMode.value === 'SIMPLE' && mode === 'JSON' && simpleValidateResult.value.error) {
    return {
      title: '切换到高级模式（JSON）',
      description: `已检测到当前简单模式有错误。
如果继续切换，高级模式会用当前简单模式内容重新生成 JSON。
你会直接看到这份带错误的 JSON。
这也会替换你之前在高级模式里未保存的 JSON 草稿。确认继续吗？`,
      confirmType: 'danger' as const,
      overwriteDraft: false,
    }
  }

  return null
}

function applyModeSwitch(mode: MatchRuleEditorMode, overwriteDraft: boolean) {
  const patch: Partial<{
    matchRule: MatchRule
    matchRuleDraft: string
    matchRuleEditorMode: MatchRuleEditorMode
  }> = {
    matchRuleEditorMode: mode,
  }
  if (mode === 'JSON' || overwriteDraft) {
    const text = formatMatchRule(props.modelValue)
    jsonDraft.value = text
    patch.matchRuleDraft = text
  }
  emit('update:state', patch)
  emit('change')
}

function updateSimple(value: MatchRule) {
  const normalized = normalizeMatchRule(value)
  const draft = formatMatchRule(normalized)
  emit('update:state', {
    matchRule: normalized,
    matchRuleDraft: draft,
    matchRuleEditorMode: 'SIMPLE',
  })
  emit('change')
}

function updateJsonDraft(value: string) {
  jsonDraft.value = value
  const patch: Partial<{
    matchRule: MatchRule
    matchRuleDraft: string
    matchRuleEditorMode: MatchRuleEditorMode
  }> = {
    matchRuleDraft: value,
    matchRuleEditorMode: 'JSON',
  }
  if (parseResult.value.rule) {
    patch.matchRule = parseResult.value.rule
  }
  emit('update:state', patch)
  emit('change')
}

function formatJson() {
  const parsed = parseResult.value.rule ?? normalizeMatchRule(props.modelValue)
  const next = formatMatchRule(parsed || makeMatchRuleGroup())
  jsonDraft.value = next
  emit('update:state', {
    matchRule: parsed || makeMatchRuleGroup(),
    matchRuleDraft: next,
    matchRuleEditorMode: 'JSON',
  })
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

    <template v-if="currentMode === 'SIMPLE'">
      <MatchRuleNodeEditor
        :model-value="normalizeMatchRule(modelValue)"
        :validation-error="simpleValidationError"
        root
        @change="emit('change')"
        @update:model-value="updateSimple"
      />
    </template>

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
      <p v-if="parseError" class=":uno: text-xs text-red-500">{{ parseError }}</p>
    </div>
  </div>
</template>

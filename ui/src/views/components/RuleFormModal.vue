<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import {
  type CodeSnippet,
  type InjectionRule,
  makeRule,
  MODE_OPTIONS,
  POSITION_OPTIONS,
} from '@/types'
import { getDomRulePerformanceWarning } from '@/views/composables/matchRule'
import BaseFormModal from './BaseFormModal.vue'
import ItemPicker from './ItemPicker.vue'
import FormField from './FormField.vue'
import MatchRuleEditor from './MatchRuleEditor.vue'

defineProps<{
  snippets: CodeSnippet[]
  saving: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', rule: InjectionRule, snippetIds: string[]): void
}>()

const rule = ref<InjectionRule>(makeRule())
const selectedSnippetIds = ref<string[]>([])

onMounted(reset)

function reset() {
  rule.value = makeRule()
  selectedSnippetIds.value = []
}

const needsTarget = computed(() => rule.value.mode === 'ID' || rule.value.mode === 'SELECTOR')
const needsSnippets = computed(() => rule.value.position !== 'REMOVE')
const needsWrapMarker = computed(() => rule.value.position !== 'REMOVE')
const performanceWarning = computed(() => getDomRulePerformanceWarning(rule.value))

watch(
  () => rule.value.position,
  (position) => {
    if (position === 'REMOVE') {
      rule.value.wrapMarker = false
    }
  },
)

function toggleSnippet(id: string) {
  const idx = selectedSnippetIds.value.indexOf(id)
  if (idx === -1) selectedSnippetIds.value.push(id)
  else selectedSnippetIds.value.splice(idx, 1)
}

function handleSubmit() {
  emit('submit', rule.value, selectedSnippetIds.value)
}
</script>

<template>
  <BaseFormModal
    :saving="saving"
    :show-picker="needsSnippets"
    title="新建注入规则"
    @close="emit('close')"
    @submit="handleSubmit"
  >
    <template #form>
      <FormField label="名称">
        <input
          v-model="rule.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
        />
      </FormField>

      <FormField label="描述">
        <input
          v-model="rule.description"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此规则的用途"
        />
      </FormField>

      <FormField label="注入模式" required>
        <select
          v-model="rule.mode"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
        >
          <option v-for="o in MODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
      </FormField>

      <template v-if="needsTarget">
        <FormField :label="rule.mode === 'SELECTOR' ? 'CSS 选择器' : '元素 ID'" required>
          <input
            v-model="rule.match"
            :placeholder="rule.mode === 'SELECTOR' ? 'div[class=content]' : 'main-content'"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none"
          />
        </FormField>

        <FormField label="插入位置">
          <select
            v-model="rule.position"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
          >
            <option v-for="o in POSITION_OPTIONS" :key="o.value" :value="o.value">
              {{ o.label }}
            </option>
          </select>
        </FormField>
      </template>

      <FormField v-if="needsWrapMarker" label="注释标记">
        <label class=":uno: inline-flex items-center gap-2 text-sm text-gray-700">
          <input v-model="rule.wrapMarker" type="checkbox" />
          输出 `<!-- PluginInjector start/end -->` 注释标记
        </label>
      </FormField>

      <FormField label="匹配规则" required>
        <MatchRuleEditor
          :draft="rule.matchRuleDraft"
          :editor-mode="rule.matchRuleEditorMode"
          :model-value="rule.matchRule"
          @change="void 0"
          @update:draft="rule.matchRuleDraft = $event"
          @update:editor-mode="rule.matchRuleEditorMode = $event"
          @update:model-value="rule.matchRule = $event"
        />
        <div
          v-if="performanceWarning"
          class=":uno: mt-2 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-5 text-amber-800"
        >
          {{ performanceWarning }}
        </div>
      </FormField>
    </template>

    <template v-if="needsSnippets" #picker>
      <div class=":uno: flex items-center justify-between">
        <label class=":uno: text-xs font-medium text-gray-600">关联代码块</label>
        <span class=":uno: text-xs text-gray-400"> {{ selectedSnippetIds.length }} 个已选 </span>
      </div>
      <ItemPicker
        :items="snippets"
        :selected-ids="selectedSnippetIds"
        empty-text="暂无代码块, 请先创建"
        @toggle="toggleSnippet"
      />
    </template>
  </BaseFormModal>
</template>

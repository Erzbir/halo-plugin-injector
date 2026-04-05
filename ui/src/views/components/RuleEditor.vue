<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { MODE_OPTIONS, POSITION_OPTIONS } from '@/types'
import { getDomRulePerformanceWarning } from '@/views/composables/matchRule'
import ItemPicker from './ItemPicker.vue'
import EditorToolbar from './EditorToolbar.vue'
import EditorFooter from './EditorFooter.vue'
import FormField from './FormField.vue'
import MatchRuleEditor from './MatchRuleEditor.vue'
import { sortSelectedFirst } from '@/views/composables/util.ts'

const props = defineProps<{
  rule: InjectionRule | null
  snippets: CodeSnippet[]
  selectedSnippetIds: string[]
  saving: boolean
  dirty: boolean
}>()

const emit = defineEmits<{
  (e: 'save'): void
  (e: 'delete'): void
  (e: 'toggle-enabled'): void
  (e: 'toggle-snippet', snippetId: string): void
  (e: 'field-change'): void
  (e: 'update:rule', rule: InjectionRule): void
}>()

const sortedSnippets = computed(() => sortSelectedFirst(props.snippets, props.selectedSnippetIds))
const pendingRule = ref<InjectionRule | null>(null)
const currentRule = computed(() => pendingRule.value ?? props.rule)

const needsTarget = computed(
  () => currentRule.value?.mode === 'ID' || currentRule.value?.mode === 'SELECTOR',
)
const needsSnippets = computed(() => currentRule.value?.position !== 'REMOVE')
const needsWrapMarker = computed(() => currentRule.value?.position !== 'REMOVE')
const performanceWarning = computed(() =>
  currentRule.value ? getDomRulePerformanceWarning(currentRule.value) : null,
)

watch(
  () => props.rule,
  () => {
    pendingRule.value = null
  },
)

function updateField<K extends keyof InjectionRule>(key: K, value: InjectionRule[K]) {
  if (!currentRule.value) return
  const next = { ...currentRule.value, [key]: value }
  if (key === 'position' && value === 'REMOVE') {
    next.wrapMarker = false
  }
  pendingRule.value = next
  emit('update:rule', next)
  emit('field-change')
}
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <EditorToolbar
      :enabled="currentRule?.enabled"
      :show-actions="!!currentRule"
      :title="currentRule ? '编辑规则' : '注入规则'"
      @delete="emit('delete')"
      @toggle-enabled="emit('toggle-enabled')"
    />

    <div v-if="!currentRule" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择规则进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <FormField label="ID">
        <input
          :value="currentRule.id"
          class=":uno: w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-1.5 text-xs font-mono text-gray-400 cursor-default"
          readonly
        />
      </FormField>

      <FormField label="名称">
        <input
          :value="currentRule.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
          @change="updateField('name', ($event.target as HTMLInputElement).value)"
        />
      </FormField>

      <FormField label="描述">
        <input
          :value="currentRule.description"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此规则的用途"
          @change="updateField('description', ($event.target as HTMLInputElement).value)"
        />
      </FormField>

      <FormField label="注入模式" required>
        <select
          :value="currentRule.mode"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
          @change="
            updateField('mode', ($event.target as HTMLSelectElement).value as InjectionRule['mode'])
          "
        >
          <option v-for="o in MODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
      </FormField>

      <template v-if="needsTarget">
        <FormField :label="currentRule.mode === 'SELECTOR' ? 'CSS 选择器' : '元素 ID'" required>
          <input
            :placeholder="currentRule.mode === 'SELECTOR' ? 'div[class=content]' : 'main-content'"
            :value="currentRule.match"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none"
            @change="updateField('match', ($event.target as HTMLInputElement).value)"
          />
        </FormField>

        <FormField label="插入位置">
          <select
            :value="currentRule.position"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
            @change="
              updateField(
                'position',
                ($event.target as HTMLSelectElement).value as InjectionRule['position'],
              )
            "
          >
            <option v-for="o in POSITION_OPTIONS" :key="o.value" :value="o.value">
              {{ o.label }}
            </option>
          </select>
        </FormField>
      </template>

      <FormField v-if="needsWrapMarker" label="注释标记">
        <label class=":uno: inline-flex items-center gap-2 text-sm text-gray-700">
          <input
            :checked="currentRule.wrapMarker"
            type="checkbox"
            @change="updateField('wrapMarker', ($event.target as HTMLInputElement).checked)"
          />
          输出注释标记
        </label>
      </FormField>

      <FormField label="匹配规则" required>
        <MatchRuleEditor
          :draft="currentRule.matchRuleDraft"
          :editor-mode="currentRule.matchRuleEditorMode"
          :model-value="currentRule.matchRule"
          @change="emit('field-change')"
          @update:draft="updateField('matchRuleDraft', $event)"
          @update:editor-mode="updateField('matchRuleEditorMode', $event)"
          @update:model-value="updateField('matchRule', $event)"
        />
        <div
          v-if="performanceWarning"
          class=":uno: mt-2 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-5 text-amber-800"
        >
          {{ performanceWarning }}
        </div>
      </FormField>

      <FormField v-if="needsSnippets" label="关联代码块">
        <template #default>
          <div class=":uno: flex items-center justify-between mb-1">
            <span />
            <span class=":uno: text-xs text-gray-400">
              {{ selectedSnippetIds.length }} 个已选
            </span>
          </div>
          <ItemPicker
            :items="sortedSnippets"
            :selected-ids="selectedSnippetIds"
            empty-text="暂无代码块, 请先创建"
            @toggle="(id) => emit('toggle-snippet', id)"
          />
        </template>
      </FormField>

      <EditorFooter :dirty="dirty" :saving="saving" @save="emit('save')" />
    </form>
  </div>
</template>

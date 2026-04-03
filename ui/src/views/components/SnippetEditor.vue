<script lang="ts" setup>
import type { CodeSnippet, InjectionRule } from '@/types'
import ItemPicker from './ItemPicker.vue'
import EditorToolbar from './EditorToolbar.vue'
import EditorFooter from './EditorFooter.vue'
import FormField from './FormField.vue'
import { rulePreview, sortSelectedFirst } from '@/views/composables/util'
import { computed } from 'vue'

const props = defineProps<{
  snippet: CodeSnippet | null
  rules: InjectionRule[]
  selectedRuleIds: string[]
  saving: boolean
  dirty: boolean
}>()

const emit = defineEmits<{
  (e: 'save'): void
  (e: 'delete'): void
  (e: 'toggle-enabled'): void
  (e: 'toggle-rule', ruleId: string): void
  (e: 'field-change'): void
  (e: 'update:snippet', snippet: CodeSnippet): void
}>()

const sortedRules = computed(() => sortSelectedFirst(props.rules, props.selectedRuleIds))

function updateField<K extends keyof CodeSnippet>(key: K, value: CodeSnippet[K]) {
  if (!props.snippet) return
  emit('update:snippet', { ...props.snippet, [key]: value })
  emit('field-change')
}
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <EditorToolbar
      :enabled="snippet?.enabled"
      :show-actions="!!snippet"
      :title="snippet ? '编辑代码块' : '代码块'"
      @delete="emit('delete')"
      @toggle-enabled="emit('toggle-enabled')"
    />

    <div v-if="!snippet" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择代码块进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <FormField label="ID">
        <input
          :value="snippet.id"
          class=":uno: w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-1.5 text-xs font-mono text-gray-400 cursor-default"
          readonly
        />
      </FormField>

      <FormField label="名称">
        <input
          :value="snippet.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
          @change="updateField('name', ($event.target as HTMLInputElement).value)"
        />
      </FormField>

      <FormField label="描述">
        <input
          :value="snippet.description"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此代码块的用途"
          @change="updateField('description', ($event.target as HTMLInputElement).value)"
        />
      </FormField>

      <FormField label="关联规则">
        <template #default>
          <div class=":uno: flex items-center justify-between mb-1">
            <span />
            <span class=":uno: text-xs text-gray-400">{{ selectedRuleIds.length }} 个已选</span>
          </div>
          <ItemPicker
            :items="sortedRules"
            :preview-fn="rulePreview"
            :selected-ids="selectedRuleIds"
            empty-text="暂无规则, 请先创建"
            @toggle="(id) => emit('toggle-rule', id)"
          />
        </template>
      </FormField>

      <FormField label="代码内容" required>
        <textarea
          :value="snippet.code"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-2 text-xs font-mono focus:border-primary focus:outline-none resize-none"
          placeholder="输入 HTML 代码"
          rows="10"
          spellcheck="false"
          @change="updateField('code', ($event.target as HTMLTextAreaElement).value)"
        />
      </FormField>

      <EditorFooter :dirty="dirty" :saving="saving" @save="emit('save')" />
    </form>
  </div>
</template>

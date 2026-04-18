<script lang="ts" setup>
import type { CodeSnippet, InjectionRule } from '@/types'
import { VButton } from '@halo-dev/components'
import EditorToolbar from './EditorToolbar.vue'
import EditorFooter from './EditorFooter.vue'
import FormField from './FormField.vue'
import RelationPicker from './RelationPicker.vue'
import CodeEditor from './CodeEditor.vue'
import { rulePreview, sortSelectedFirst } from '@/views/composables/util'
import { computed } from 'vue'

const props = defineProps<{
  snippet: CodeSnippet | null
  rules: InjectionRule[]
  selectedRuleIds: string[]
  saving: boolean
  dirty: boolean
  dirtyFields: Partial<Record<keyof CodeSnippet | 'ruleIds', boolean>>
}>()

const emit = defineEmits<{
  (e: 'save'): void
  (e: 'delete'): void
  (e: 'toggle-enabled'): void
  (e: 'toggle-rule', ruleId: string): void
  (e: 'field-change', field: keyof CodeSnippet | 'ruleIds'): void
  (e: 'revert-field', field: keyof CodeSnippet | 'ruleIds'): void
  (e: 'revert-all'): void
  (e: 'update:snippet', snippet: CodeSnippet): void
}>()

const sortedRules = computed(() => sortSelectedFirst(props.rules, props.selectedRuleIds))

function updateField<K extends keyof CodeSnippet>(key: K, value: CodeSnippet[K]) {
  if (!props.snippet) return
  emit('update:snippet', { ...props.snippet, [key]: value })
  emit('field-change', key)
}
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <EditorToolbar
      :enabled="snippet?.enabled"
      :display-id="snippet?.id"
      :show-actions="!!snippet"
      :title="snippet ? '编辑代码片段' : '代码片段'"
      @delete="emit('delete')"
      @toggle-enabled="emit('toggle-enabled')"
    />

    <div v-if="!snippet" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择代码片段进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <FormField label="名称">
        <template #action>
          <VButton
            :class="dirtyFields.name ? '' : ':uno: invisible pointer-events-none'"
            size="xs"
            @click="emit('revert-field', 'name')"
          >
            撤销修改
          </VButton>
        </template>
        <input
          :value="snippet.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
          @change="updateField('name', ($event.target as HTMLInputElement).value)"
        />
      </FormField>

      <FormField label="描述">
        <template #action>
          <VButton
            :class="dirtyFields.description ? '' : ':uno: invisible pointer-events-none'"
            size="xs"
            @click="emit('revert-field', 'description')"
          >
            撤销修改
          </VButton>
        </template>
        <textarea
          rows="1"
          :value="snippet.description"
          class=":uno: w-full min-h-[34px] resize-y rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此代码片段的用途"
          @change="updateField('description', ($event.target as HTMLTextAreaElement).value)"
        ></textarea>
      </FormField>

      <FormField label="关联规则">
        <template #action>
          <VButton
            :class="dirtyFields.ruleIds ? '' : ':uno: invisible pointer-events-none'"
            size="xs"
            @click="emit('revert-field', 'ruleIds')"
          >
            撤销修改
          </VButton>
        </template>
        <RelationPicker
          label="关联规则"
          :show-label="false"
          :items="sortedRules"
          :preview-fn="rulePreview"
          :selected-ids="selectedRuleIds"
          empty-text="暂无规则, 请先创建"
          @toggle="(id) => emit('toggle-rule', id)"
        />
      </FormField>

      <FormField label="代码内容" required>
        <template #action>
          <VButton
            :class="dirtyFields.code ? '' : ':uno: invisible pointer-events-none'"
            size="xs"
            @click="emit('revert-field', 'code')"
          >
            撤销修改
          </VButton>
        </template>
        <CodeEditor
          :model-value="snippet.code"
          :invalid="!snippet.code.trim()"
          placeholder="输入 HTML 代码"
          :rows="10"
          @update:model-value="updateField('code', $event)"
        />
      </FormField>

      <EditorFooter :dirty="dirty" :saving="saving" @revert-all="emit('revert-all')" @save="emit('save')" />
    </form>
  </div>
</template>

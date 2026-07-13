<script lang="ts" setup>
import type { CodeSnippet, InjectionRule } from '@/types'
import { VButton } from '@halo-dev/components'
import EditorToolbar from './EditorToolbar.vue'
import FormField from './FormField.vue'
import RelationPicker from './RelationPicker.vue'
import SnippetFields from './SnippetFields.vue'
import { rulePreview, sortSelectedFirst } from '@/views/composables/util'
import { computed } from 'vue'

const props = defineProps<{
  snippet: CodeSnippet | null
  rules: InjectionRule[]
  selectedRuleIds: string[]
  saving: boolean
  dirty: boolean
  relationCount: number
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
  (e: 'open-relations'): void
}>()

const sortedRules = computed(() => sortSelectedFirst(props.rules, props.selectedRuleIds))
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <EditorToolbar
      :enabled="snippet?.enabled"
      :display-id="snippet?.id"
      :show-actions="!!snippet"
      :title="snippet ? '编辑代码片段' : '代码片段'"
      :dirty="dirty"
      :saving="saving"
      :relation-count="relationCount"
      @delete="emit('delete')"
      @revert-all="emit('revert-all')"
      @save="emit('save')"
      @toggle-enabled="emit('toggle-enabled')"
      @open-relations="emit('open-relations')"
    />

    <div v-if="!snippet" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择代码片段进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <SnippetFields
        :snippet="snippet"
        :dirty-fields="dirtyFields"
        :show-revert="true"
        :code-rows="8"
        @field-change="emit('field-change', $event)"
        @revert-field="emit('revert-field', $event)"
        @update:snippet="emit('update:snippet', $event)"
      />

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

    </form>
  </div>
</template>

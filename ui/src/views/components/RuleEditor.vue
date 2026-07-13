<script lang="ts" setup>
import { computed } from 'vue'
import { VButton } from '@halo-dev/components'
import type { CodeSnippet, InjectionRule } from '@/types'
import EditorToolbar from './EditorToolbar.vue'
import FormField from './FormField.vue'
import RuleFields from './RuleFields.vue'
import RelationPicker from './RelationPicker.vue'
import { sortSelectedFirst } from '@/views/composables/util.ts'

const props = defineProps<{
  rule: InjectionRule | null
  snippets: CodeSnippet[]
  selectedSnippetIds: string[]
  saving: boolean
  dirty: boolean
  relationCount: number
  dirtyFields: Partial<Record<keyof InjectionRule | 'snippetIds', boolean>>
}>()

const emit = defineEmits<{
  (e: 'save'): void
  (e: 'delete'): void
  (e: 'set-enabled', enabled: boolean): void
  (e: 'toggle-snippet', snippetId: string): void
  (e: 'field-change', field: keyof InjectionRule | 'snippetIds'): void
  (e: 'revert-field', field: keyof InjectionRule | 'snippetIds'): void
  (e: 'revert-all'): void
  (e: 'update:rule', rule: InjectionRule): void
  (e: 'open-relations'): void
}>()

const sortedSnippets = computed(() => sortSelectedFirst(props.snippets, props.selectedSnippetIds))
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <EditorToolbar
      :enabled="rule?.enabled"
      :display-id="rule?.id"
      :show-actions="!!rule"
      :title="rule ? '编辑规则' : '注入规则'"
      :dirty="dirty"
      :saving="saving"
      :relation-count="relationCount"
      @delete="emit('delete')"
      @revert-all="emit('revert-all')"
      @save="emit('save')"
      @set-enabled="emit('set-enabled', $event)"
      @open-relations="emit('open-relations')"
    />

    <div v-if="!rule" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择规则进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <RuleFields
        :rule="rule"
        :dirty-fields="dirtyFields"
        @change="emit('field-change', 'matchRule')"
        @revert-field="emit('revert-field', $event)"
        @update:rule="emit('update:rule', $event)"
      />

      <FormField label="关联代码片段">
        <template #action>
          <VButton
            :class="dirtyFields.snippetIds ? '' : ':uno: invisible pointer-events-none'"
            size="xs"
            @click="emit('revert-field', 'snippetIds')"
          >
            撤销修改
          </VButton>
        </template>
        <RelationPicker
          label="关联代码片段"
          :show-label="false"
          :items="sortedSnippets"
          :selected-ids="selectedSnippetIds"
          empty-text="暂无代码片段, 请先创建"
          @toggle="(id) => emit('toggle-snippet', id)"
        />
      </FormField>
    </form>
  </div>
</template>

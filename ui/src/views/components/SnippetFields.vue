<script lang="ts" setup>
import { VButton } from '@halo-dev/components'
import type { CodeSnippet } from '@/types'
import FormField from './FormField.vue'
import CodeEditor from './CodeEditor.vue'

const props = withDefaults(
  defineProps<{
    snippet: CodeSnippet
    dirtyFields?: Partial<Record<keyof CodeSnippet, boolean>>
    showRevert?: boolean
    codeRows?: number
    autofocusCode?: boolean
  }>(),
  {
    showRevert: false,
    codeRows: 8,
    autofocusCode: false,
  },
)

const emit = defineEmits<{
  (e: 'update:snippet', snippet: CodeSnippet): void
  (e: 'field-change', field: keyof CodeSnippet): void
  (e: 'revert-field', field: keyof CodeSnippet): void
}>()

function updateField<K extends keyof CodeSnippet>(key: K, value: CodeSnippet[K]) {
  emit('update:snippet', { ...props.snippet, [key]: value })
  emit('field-change', key)
}
</script>

<template>
  <FormField label="名称">
    <template v-if="showRevert" #action>
      <VButton
        :class="dirtyFields?.name ? '' : ':uno: invisible pointer-events-none'"
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
    <template v-if="showRevert" #action>
      <VButton
        :class="dirtyFields?.description ? '' : ':uno: invisible pointer-events-none'"
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

  <FormField label="代码内容" required>
    <template v-if="showRevert" #action>
      <VButton
        :class="dirtyFields?.code ? '' : ':uno: invisible pointer-events-none'"
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
      :rows="codeRows"
      :autofocus="autofocusCode"
      @update:model-value="updateField('code', $event)"
    />
  </FormField>
</template>

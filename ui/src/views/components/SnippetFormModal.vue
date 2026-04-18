<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { makeSnippet } from '@/types'
import BaseFormModal from './BaseFormModal.vue'
import RelationPicker from './RelationPicker.vue'
import FormField from './FormField.vue'
import CodeEditor from './CodeEditor.vue'
import { rulePreview } from '@/views/composables/util'

defineProps<{
  rules: InjectionRule[]
  saving: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', snippet: CodeSnippet, ruleIds: string[]): void
}>()

const snippet = ref<CodeSnippet>(makeSnippet())
const selectedRuleIds = ref<string[]>([])

onMounted(reset)

function reset() {
  snippet.value = makeSnippet()
  selectedRuleIds.value = []
}

function toggleRule(id: string) {
  const idx = selectedRuleIds.value.indexOf(id)
  if (idx === -1) selectedRuleIds.value.push(id)
  else selectedRuleIds.value.splice(idx, 1)
}

function handleSubmit() {
  emit('submit', snippet.value, selectedRuleIds.value)
}
</script>

<template>
  <BaseFormModal :saving="saving" title="新建代码片段" @close="emit('close')" @submit="handleSubmit">
    <template #form>
      <FormField label="名称">
        <input
          v-model="snippet.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
        />
      </FormField>

      <FormField label="描述">
        <textarea
          rows="1"
          v-model="snippet.description"
          class=":uno: w-full min-h-[34px] resize-y rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此代码片段的用途"
        ></textarea>
      </FormField>

      <FormField label="代码内容" required>
        <CodeEditor
          :model-value="snippet.code"
          :invalid="!snippet.code.trim()"
          placeholder="输入 HTML 代码"
          :rows="12"
          autofocus
          @update:model-value="snippet.code = $event"
        />
      </FormField>
    </template>

    <template #picker>
      <RelationPicker
        label="关联规则"
        :items="rules"
        :preview-fn="rulePreview"
        :selected-ids="selectedRuleIds"
        empty-text="暂无规则, 请先创建"
        @toggle="toggleRule"
      />
    </template>
  </BaseFormModal>
</template>

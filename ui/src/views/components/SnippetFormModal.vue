<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { makeSnippet } from '@/types'
import BaseFormModal from './BaseFormModal.vue'
import RelationPicker from './RelationPicker.vue'
import SnippetFields from './SnippetFields.vue'
import { rulePreview } from '@/views/composables/util'
import { isSameJson } from '@/views/composables/injectorDataUtils'

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
const dirty = computed(
  () => !isSameJson(snippet.value, makeSnippet()) || selectedRuleIds.value.length > 0,
)

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
  <BaseFormModal
    :saving="saving"
    :dirty="dirty"
    title="新建代码片段"
    @close="emit('close')"
    @submit="handleSubmit"
  >
    <template #form>
      <SnippetFields
        :snippet="snippet"
        :show-revert="false"
        :code-rows="12"
        :autofocus-code="true"
        @update:snippet="snippet = $event"
      />
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

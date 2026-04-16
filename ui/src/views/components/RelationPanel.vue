<script lang="ts" setup>
import { computed } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import ItemListV from './ItemListV.vue'
import { rulePreview } from '@/views/composables/util'

const props = defineProps<{
  mode: 'snippets' | 'rules'
  selectedSnippetId: string | null
  selectedRuleId: string | null
  rulesUsingSnippet: InjectionRule[]
  snippetsInRule: CodeSnippet[]
}>()

const emit = defineEmits<{
  (e: 'jump-to-rule', id: string): void
  (e: 'jump-to-snippet', id: string): void
}>()

const hasSelection = computed(() =>
  props.mode === 'snippets' ? !!props.selectedSnippetId : !!props.selectedRuleId,
)
const headerTitle = computed(() => {
  if (props.mode === 'snippets') return `被 ${props.rulesUsingSnippet.length} 个规则引用`
  return `关联 ${props.snippetsInRule.length} 个代码片段`
})
const headerPlaceholder = computed(() =>
  props.mode === 'snippets' ? '选择一个代码片段' : '选择一个规则',
)
const emptyText = computed(() =>
  props.mode === 'snippets' ? '该代码片段暂未被任何规则引用' : '该规则暂未关联代码片段',
)
const hintText = computed(() =>
  props.mode === 'snippets' ? '点击跳转到规则 →' : '点击跳转到代码片段 →',
)
const ruleRelationItems = computed(() => props.rulesUsingSnippet)
const snippetRelationItems = computed(() => props.snippetsInRule)

function handleSelect(id: string) {
  if (props.mode === 'snippets') {
    emit('jump-to-rule', id)
    return
  }
  emit('jump-to-snippet', id)
}
</script>

<template>
  <div class=":uno: h-full flex flex-col">
    <div class=":uno: sticky top-0 z-10 h-12 flex items-center border-b bg-white px-4 shrink-0">
      <h2 v-if="hasSelection" class=":uno: text-sm font-semibold text-gray-900">{{ headerTitle }}</h2>
      <span v-else class=":uno: text-sm text-gray-400">{{ headerPlaceholder }}</span>
    </div>

    <div class=":uno: flex-1 overflow-y-auto">
      <ItemListV
        v-if="hasSelection && mode === 'snippets'"
        :items="ruleRelationItems"
        :empty-text="emptyText"
        @select="handleSelect"
      >
        <template #meta="{ item }">
          <span class=":uno: text-xs text-gray-500">{{ rulePreview(item) }}</span>
        </template>
        <template #hint>
          <span class=":uno: text-xs text-primary opacity-0 mt-0.5 group-hover:opacity-100 transition-opacity">
            {{ hintText }}
          </span>
        </template>
      </ItemListV>
      <ItemListV
        v-else-if="hasSelection"
        :items="snippetRelationItems"
        :empty-text="emptyText"
        @select="handleSelect"
      >
        <template #hint>
          <span class=":uno: text-xs text-primary opacity-0 mt-0.5 group-hover:opacity-100 transition-opacity">
            {{ hintText }}
          </span>
        </template>
      </ItemListV>
    </div>
  </div>
</template>

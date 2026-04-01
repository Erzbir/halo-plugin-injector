<script lang="ts" setup>
import type { CodeSnippet, InjectionRule } from '@/types'
import { VButton, VSpace } from '@halo-dev/components'
import ItemPicker from '../../components/ItemPicker.vue'
import { rulePreview } from '@/views/composables/util.ts'

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
  (e: 'update:snippet', rule: CodeSnippet): void
}>()

function updateField<K extends keyof CodeSnippet>(key: K, value: CodeSnippet[K]) {
  if (!props.snippet) return
  emit('update:snippet', { ...props.snippet, [key]: value })
  emit('field-change')
}
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <div
      class=":uno: sticky top-0 z-10 h-12 flex items-center justify-between border-b bg-white px-4 shrink-0"
    >
      <h2 class=":uno: text-gray-900 font-semibold text-sm">
        {{ snippet ? '编辑代码块' : '代码块' }}
      </h2>
      <VSpace v-if="snippet">
        <VButton size="sm" @click="emit('toggle-enabled')">
          {{ snippet.enabled ? '禁用' : '启用' }}
        </VButton>
        <VButton size="sm" type="danger" @click="emit('delete')">删除</VButton>
      </VSpace>
    </div>

    <div v-if="!snippet" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择代码块进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">ID</label>
        <input
          :value="snippet.id"
          class=":uno: w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-1.5 text-xs font-mono text-gray-400 cursor-default"
          readonly
        />
      </div>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">名称</label>
        <input
          :value="snippet.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
          @change="
            updateField('name', ($event.target as HTMLSelectElement).value as CodeSnippet['name'])
          "
        />
      </div>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">描述</label>
        <input
          :value="snippet.description"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此代码块的用途"
          @change="
            updateField(
              'description',
              ($event.target as HTMLSelectElement).value as CodeSnippet['description'],
            )
          "
        />
      </div>

      <div class=":uno: space-y-1">
        <div class=":uno: flex items-center justify-between">
          <label class=":uno: text-xs font-medium text-gray-600">关联规则</label>
          <span class=":uno: text-xs text-gray-400">{{ selectedRuleIds.length }} 个已选</span>
        </div>
        <ItemPicker
          :items="rules"
          :preview-fn="rulePreview"
          :selected-ids="selectedRuleIds"
          empty-text="暂无规则, 请先创建"
          @toggle="(id) => emit('toggle-rule', id)"
        />
      </div>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">
          代码内容 <span class=":uno: text-red-500">*</span>
        </label>
        <textarea
          :value="snippet.code"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-2 text-xs font-mono focus:border-primary focus:outline-none resize-none"
          placeholder="输入 HTML 代码"
          rows="10"
          spellcheck="false"
          @change="
            updateField('code', ($event.target as HTMLSelectElement).value as CodeSnippet['code'])
          "
        />
      </div>

      <div class=":uno: flex items-center justify-between pt-1 pb-2">
        <span v-if="dirty" class=":uno: text-xs text-yellow-600">有未保存的修改</span>
        <span v-else />
        <VButton :disabled="saving" type="secondary" @click="emit('save')">
          {{ saving ? '保存中...' : '保存' }}
        </VButton>
      </div>
    </form>
  </div>
</template>

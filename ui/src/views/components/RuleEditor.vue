<script lang="ts" setup>
import { computed } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { MODE_OPTIONS, POSITION_OPTIONS } from '@/types'
import { VButton, VSpace } from '@halo-dev/components'
import ItemPicker from '../../components/ItemPicker.vue'
import PathPatternEditor from '../../components/PathPatternEditor.vue'
import { codePreview } from '@/views/composables/util.ts'

const props = defineProps<{
  rule: InjectionRule | null
  snippets: CodeSnippet[]
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

const needsSelectorOrId = computed(
  () => props.rule?.mode === 'ID' || props.rule?.mode === 'SELECTOR',
)

function updateField<K extends keyof InjectionRule>(key: K, value: InjectionRule[K]) {
  if (!props.rule) return
  emit('update:rule', { ...props.rule, [key]: value })
  emit('field-change')
}
</script>

<template>
  <div class=":uno: h-full flex flex-col injector-editor-container">
    <div
      class=":uno: sticky top-0 z-10 h-12 flex items-center justify-between border-b bg-white px-4 shrink-0"
    >
      <h2 class=":uno: text-gray-900 font-semibold text-sm">
        {{ rule ? '编辑规则' : '注入规则' }}
      </h2>
      <VSpace v-if="rule">
        <VButton size="sm" @click="emit('toggle-enabled')">
          {{ rule.enabled ? '禁用' : '启用' }}
        </VButton>
        <VButton size="sm" type="danger" @click="emit('delete')">删除</VButton>
      </VSpace>
    </div>

    <div v-if="!rule" class=":uno: flex flex-1 items-center justify-center">
      <span class=":uno: text-sm text-gray-500">从左侧选择规则进行编辑</span>
    </div>

    <form
      v-else
      class=":uno: flex-1 overflow-y-auto px-4 py-4 space-y-4"
      @submit.prevent="emit('save')"
    >
      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">ID</label>
        <input
          :value="rule.id"
          class=":uno: w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-1.5 text-xs font-mono text-gray-400 cursor-default"
          readonly
        />
      </div>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">名称</label>
        <input
          :value="rule.name"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="不填默认为 ID"
          @change="updateField('name', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">描述</label>
        <input
          :value="rule.description"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
          placeholder="说明此规则的用途"
          @change="updateField('description', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">
          注入模式 <span class=":uno: text-red-500">*</span>
        </label>
        <select
          :value="rule.mode"
          class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
          @change="
            updateField('mode', ($event.target as HTMLSelectElement).value as InjectionRule['mode'])
          "
        >
          <option v-for="o in MODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
      </div>

      <template v-if="needsSelectorOrId">
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">
            {{ rule.mode === 'SELECTOR' ? 'CSS 选择器' : '元素 ID' }}
            <span class=":uno: text-red-500">*</span>
          </label>
          <input
            :placeholder="rule.mode === 'SELECTOR' ? 'div[class=content]' : 'main-content'"
            :value="rule.match"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none"
            @change="updateField('match', ($event.target as HTMLInputElement).value)"
          />
        </div>
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">插入位置</label>
          <select
            :value="rule.position"
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
        </div>
      </template>

      <div class=":uno: space-y-1">
        <label class=":uno: text-xs font-medium text-gray-600">
          路径规则 <span class=":uno: text-red-500">*</span>
        </label>
        <PathPatternEditor
          :model-value="rule.pathPatterns"
          @change="emit('field-change')"
          @update:model-value="updateField('pathPatterns', $event)"
        />
      </div>

      <div class=":uno: space-y-1">
        <div class=":uno: flex items-center justify-between">
          <label class=":uno: text-xs font-medium text-gray-600">关联代码块</label>
          <span class=":uno: text-xs text-gray-400"
            >{{ (rule.snippetIds ?? []).length }} 个已选</span
          >
        </div>
        <ItemPicker
          :items="snippets"
          :preview-fn="(s) => codePreview(s.code)"
          :selected-ids="rule.snippetIds ?? []"
          empty-text="暂无代码块，请先创建"
          @toggle="(id) => emit('toggle-snippet', id)"
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

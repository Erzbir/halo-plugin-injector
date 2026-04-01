<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { makeSnippet } from '@/types'
import { VButton, VModal, VSpace } from '@halo-dev/components'
import ItemPicker from '../../components/ItemPicker.vue'
import { rulePreview } from '@/views/composables/util.ts'

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
</script>

<template>
  <VModal :width="1000" title="新建代码块" @close="emit('close')">
    <div
      class=":uno: flex divide-x divide-gray-100 injector-editor-container"
      style="min-height: 400px"
    >
      <div class=":uno: flex-1 px-5 py-4 space-y-4 overflow-y-auto" style="width: 60%">
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">名称</label>
          <input
            v-model="snippet.name"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
            placeholder="不填默认为 ID"
          />
        </div>
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">描述</label>
          <input
            v-model="snippet.description"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
            placeholder="说明此代码块的用途"
          />
        </div>
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">
            代码内容 <span class=":uno: text-red-500">*</span>
          </label>
          <textarea
            v-model="snippet.code"
            autofocus
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-2 text-xs font-mono focus:border-primary focus:outline-none resize-none"
            placeholder="输入 HTML 代码"
            rows="12"
            spellcheck="false"
          />
        </div>
      </div>

      <div class=":uno: w-56 flex-none px-4 py-4 space-y-2 overflow-y-auto" style="width: 40%">
        <div class=":uno: flex items-center justify-between">
          <label class=":uno: text-xs font-medium text-gray-600">关联规则</label>
          <span class=":uno: text-xs text-gray-400">{{ selectedRuleIds.length }} 个已选</span>
        </div>
        <ItemPicker
          :items="rules"
          :preview-fn="rulePreview"
          :selected-ids="selectedRuleIds"
          empty-text="暂无规则，请先创建"
          @toggle="toggleRule"
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton @click="emit('close')">取消</VButton>
        <VButton
          :disabled="saving"
          type="secondary"
          @click="emit('submit', snippet, selectedRuleIds)"
        >
          {{ saving ? '创建中...' : '创建' }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

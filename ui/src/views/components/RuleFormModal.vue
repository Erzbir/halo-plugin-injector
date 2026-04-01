<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import type { CodeSnippet, InjectionRule } from '@/types'
import { makeRule, MODE_OPTIONS, POSITION_OPTIONS } from '@/types'
import { VButton, VModal, VSpace } from '@halo-dev/components'
import ItemPicker from '../../components/ItemPicker.vue'
import PathPatternEditor from '../../components/PathPatternEditor.vue'
import { codePreview } from '@/views/composables/util.ts'

defineProps<{
  snippets: CodeSnippet[]
  saving: boolean
}>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', rule: InjectionRule): void
}>()

const rule = ref<InjectionRule>(makeRule())

onMounted(() => {
  rule.value = makeRule()
})

const needsSelectorOrId = computed(() => rule.value.mode === 'ID' || rule.value.mode === 'SELECTOR')

function toggleSnippet(id: string) {
  const ids = rule.value.snippetIds ?? []
  const idx = ids.indexOf(id)
  rule.value.snippetIds = idx === -1 ? [...ids, id] : ids.filter((n) => n !== id)
}
</script>

<template>
  <VModal :width="1000" title="新建注入规则" @close="emit('close')">
    <div
      class=":uno: flex divide-x divide-gray-100 injector-editor-container"
      style="min-height: 400px"
    >
      <div class=":uno: flex-1 px-5 py-4 space-y-4 overflow-y-auto" style="width: 60%">
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">名称</label>
          <input
            v-model="rule.name"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
            placeholder="不填默认为 ID"
          />
        </div>
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">描述</label>
          <input
            v-model="rule.description"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
            placeholder="说明此规则的用途"
          />
        </div>
        <div class=":uno: space-y-1">
          <label class=":uno: text-xs font-medium text-gray-600">
            注入模式 <span class=":uno: text-red-500">*</span>
          </label>
          <select
            v-model="rule.mode"
            class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
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
              v-model="rule.match"
              :placeholder="rule.mode === 'SELECTOR' ? 'div[class=content]' : 'main-content'"
              class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none"
            />
          </div>
          <div class=":uno: space-y-1">
            <label class=":uno: text-xs font-medium text-gray-600">插入位置</label>
            <select
              v-model="rule.position"
              class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none bg-white"
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
          <PathPatternEditor v-model="rule.pathPatterns" />
        </div>
      </div>

      <div class=":uno: w-56 flex-none px-4 py-4 space-y-2 overflow-y-auto" style="width: 40%">
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
          empty-text="暂无代码块, 请先创建"
          @toggle="toggleSnippet"
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton @click="emit('close')">取消</VButton>
        <VButton :disabled="saving" type="secondary" @click="emit('submit', rule)">
          {{ saving ? '创建中...' : '创建' }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

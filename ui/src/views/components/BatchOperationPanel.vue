<script lang="ts" setup>
import { computed } from 'vue'
import { VButton } from '@halo-dev/components'
import type { ActiveTab } from '@/types'
import type { BatchActionResult } from '@/views/composables/useInjectorData'
import StatusDot from './StatusDot.vue'

type BatchItem = {
  id: string
  name: string
  description?: string
  enabled: boolean
}

const props = defineProps<{
  activeTab: ActiveTab
  items: BatchItem[]
  saving: boolean
  result: (BatchActionResult & { action: string }) | null
}>()

const emit = defineEmits<{
  (e: 'enable'): void
  (e: 'disable'): void
  (e: 'delete'): void
  (e: 'remove', id: string): void
  (e: 'clear'): void
}>()

const enabledCount = computed(() => props.items.filter((item) => item.enabled).length)
const disabledCount = computed(() => props.items.length - enabledCount.value)
const kind = computed(() => (props.activeTab === 'snippets' ? '代码片段' : '规则'))
</script>

<template>
  <div class=":uno: h-full min-h-0 flex flex-col">
    <div class=":uno: min-h-12 flex items-center justify-between border-b bg-white px-4 py-2 shrink-0">
      <div>
        <h2 class=":uno: text-gray-900 font-semibold text-sm">批量操作</h2>
        <p class=":uno: mt-0.5 text-xs text-gray-500">集中检查选择范围后再执行操作</p>
      </div>
      <VButton v-if="items.length" size="sm" @click="emit('clear')">清空选择</VButton>
    </div>

    <div class=":uno: min-h-0 flex-1 overflow-y-auto p-4 space-y-4">
      <div
        v-if="result"
        :class="
          result.failedIds.length
            ? ':uno: border-red-200 bg-red-50 text-red-700'
            : ':uno: border-green-200 bg-green-50 text-green-700'
        "
        class=":uno: rounded-md border px-4 py-3 text-sm"
        role="status"
      >
        {{ result.action }}完成, 成功 {{ result.succeededIds.length }} 项, 失败
        {{ result.failedIds.length }} 项
      </div>

      <div v-if="!items.length" class=":uno: min-h-64 flex items-center justify-center">
        <div class=":uno: max-w-sm text-center">
          <p class=":uno: text-sm font-medium text-gray-700">尚未选择{{ kind }}</p>
          <p class=":uno: mt-1 text-xs text-gray-500">从左侧勾选需要批量处理的项目</p>
        </div>
      </div>

      <template v-else>
        <div class=":uno: grid grid-cols-3 gap-3">
          <div class=":uno: rounded-md border border-gray-200 bg-white p-3">
            <p class=":uno: text-xs text-gray-500">已选择</p>
            <p class=":uno: mt-1 text-xl font-semibold text-gray-900">{{ items.length }}</p>
          </div>
          <div class=":uno: rounded-md border border-gray-200 bg-white p-3">
            <p class=":uno: text-xs text-gray-500">已启用</p>
            <p class=":uno: mt-1 text-xl font-semibold text-green-600">{{ enabledCount }}</p>
          </div>
          <div class=":uno: rounded-md border border-gray-200 bg-white p-3">
            <p class=":uno: text-xs text-gray-500">已停用</p>
            <p class=":uno: mt-1 text-xl font-semibold text-gray-600">{{ disabledCount }}</p>
          </div>
        </div>

        <section class=":uno: rounded-md border border-gray-200 bg-white">
          <div class=":uno: flex flex-wrap items-center justify-between gap-2 border-b px-4 py-3">
            <div>
              <h3 class=":uno: text-sm font-medium text-gray-800">操作预览</h3>
              <p class=":uno: mt-0.5 text-xs text-gray-500">
                以下操作将应用到当前选择的 {{ items.length }} 个{{ kind }}
              </p>
            </div>
            <div class=":uno: flex items-center gap-2">
              <VButton :disabled="saving" size="sm" @click="emit('enable')">启用</VButton>
              <VButton :disabled="saving" size="sm" @click="emit('disable')">停用</VButton>
              <VButton :disabled="saving" size="sm" type="danger" @click="emit('delete')">
                删除
              </VButton>
            </div>
          </div>

          <ul class=":uno: max-h-80 divide-y divide-gray-100 overflow-y-auto">
            <li v-for="item in items" :key="item.id" class=":uno: flex items-center gap-3 px-4 py-3">
              <StatusDot :enabled="item.enabled" />
              <div class=":uno: min-w-0 flex-1">
                <p class=":uno: truncate text-sm font-medium text-gray-900">
                  {{ item.name || item.id }}
                </p>
                <p class=":uno: truncate text-xs text-gray-500">{{ item.description || item.id }}</p>
              </div>
              <VButton size="xs" @click="emit('remove', item.id)">移除</VButton>
            </li>
          </ul>
        </section>
      </template>
    </div>
  </div>
</template>

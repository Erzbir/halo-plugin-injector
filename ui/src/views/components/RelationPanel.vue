<script lang="ts" setup>
import type { CodeSnippet, InjectionRule } from '@/types'
import { codePreview, rulePreview } from '@/views/composables/util.ts'

defineProps<{
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
</script>

<template>
  <div class=":uno: h-full flex flex-col">
    <div class=":uno: sticky top-0 z-10 h-12 flex items-center border-b bg-white px-4 shrink-0">
      <template v-if="mode === 'snippets'">
        <h2 v-if="selectedSnippetId" class=":uno: text-sm font-semibold text-gray-900">
          被
          <span class=":uno: text-primary">{{ rulesUsingSnippet.length }}</span>
          个规则引用
        </h2>
        <span v-else class=":uno: text-sm text-gray-400">选择一个代码块</span>
      </template>
      <template v-else>
        <h2 v-if="selectedRuleId" class=":uno: text-sm font-semibold text-gray-900">
          关联
          <span class=":uno: text-primary">{{ snippetsInRule.length }}</span>
          个代码块
        </h2>
        <span v-else class=":uno: text-sm text-gray-400">选择一个规则</span>
      </template>
    </div>

    <div class=":uno: flex-1 overflow-y-auto">
      <template v-if="mode === 'snippets'">
        <div
          v-if="!selectedSnippetId"
          class=":uno: flex flex-1 items-center justify-center py-10 px-4 text-center"
        >
          <span class=":uno: text-sm text-gray-400">选择左侧代码块查看引用它的规则</span>
        </div>
        <div
          v-else-if="!rulesUsingSnippet.length"
          class=":uno: flex items-center justify-center py-10 px-4 text-center"
        >
          <span class=":uno: text-sm text-gray-400">该代码块暂未被任何规则引用</span>
        </div>
        <ul v-else class=":uno: divide-y divide-gray-100">
          <li
            v-for="r in rulesUsingSnippet"
            :key="r.id"
            class=":uno: px-1.5 py-3 cursor-pointer hover:bg-gray-50 transition-colors group"
            @click="emit('jump-to-rule', r.id)"
          >
            <div class=":uno: flex items-center justify-between gap-2">
              <span class=":uno: flex-1 min-w-0 text-sm text-gray-900 font-medium truncate">
                {{ r.name || r.id }}
              </span>
              <span
                :class="r.enabled ? ':uno: bg-primary' : ':uno: bg-gray-500'"
                :title="r.enabled ? '已启用' : '已停用'"
                class=":uno: shrink-0 w-1.5 h-1.5 rounded-full"
              />
            </div>
            <p v-if="r.description" class=":uno: text-xs text-gray-500 mb-1.5 line-clamp-1">
              {{ r.description }}
            </p>
            <div class=":uno: flex flex-wrap gap-1 mb-1.5">
              <span class=":uno: text-xs text-gray-500 py-0.5 rounded">
                {{ rulePreview(r) }}
              </span>
            </div>
            <div class=":uno: flex flex-wrap gap-1">
              <span
                v-for="p in r.pathPatterns ?? []"
                :key="p.pathPattern"
                class=":uno: text-xs text-gray-500 px-1 py-0.5 mr-1 mt-1 rounded border"
              >
                {{ p.pathPattern }}
              </span>
            </div>
            <p
              class=":uno: text-xs text-primary opacity-0 mt-1.5 group-hover:opacity-100 transition-colors"
            >
              点击跳转到规则 →
            </p>
          </li>
        </ul>
      </template>

      <template v-else>
        <div
          v-if="!selectedRuleId"
          class=":uno: flex items-center justify-center py-10 px-4 text-center"
        >
          <span class=":uno: text-sm text-gray-400">选择左侧规则查看关联的代码块</span>
        </div>
        <div
          v-else-if="!snippetsInRule.length"
          class=":uno: flex items-center justify-center py-10 px-4 text-center"
        >
          <span class=":uno: text-sm text-gray-400">该规则暂未关联代码块，请在编辑面板中添加</span>
        </div>
        <ul v-else class=":uno: divide-y divide-gray-100">
          <li
            v-for="s in snippetsInRule"
            :key="s.id"
            class=":uno: px-1.5 py-3 cursor-pointer hover:bg-gray-50 transition-colors group"
            @click="emit('jump-to-snippet', s.id)"
          >
            <div class=":uno: flex flex-col py-2.5 gap-1 hover:bg-gray-50">
              <div class=":uno: flex items-center justify-between gap-2">
                <span class=":uno: flex-1 min-w-0 text-sm text-gray-900 font-medium truncate">
                  {{ s.name || s.id }}
                </span>
                <span
                  :class="s.enabled ? ':uno: bg-primary' : ':uno: bg-gray-500'"
                  :title="s.enabled ? '已启用' : '已停用'"
                  class=":uno: shrink-0 w-1.5 h-1.5 rounded-full"
                />
              </div>
            </div>
            <p v-if="s.description" class=":uno: text-xs text-gray-500 mb-1 line-clamp-1">
              {{ s.description }}
            </p>
            <p class=":uno: text-xs text-gray-400 font-mono line-clamp-2">
              {{ codePreview(s.code) }}
            </p>
            <p
              class=":uno: text-xs text-primary opacity-0 mt-1.5 group-hover:opacity-100 transition-colors"
            >
              点击跳转到代码块 →
            </p>
          </li>
        </ul>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { VButton } from '@halo-dev/components'
import type { MatchRule } from '@/types'
import {
  MATCH_RULE_GROUP_OPTIONS,
  PATH_MATCHER_OPTIONS,
  TEMPLATE_MATCHER_OPTIONS,
  makeMatchRuleGroup,
  makePathMatchRule,
  makeTemplateMatchRule,
} from '@/types'
import { cloneMatchRule, normalizeMatchRule } from '@/views/composables/matchRule'

defineOptions({
  name: 'MatchRuleNodeEditor',
})

const props = defineProps<{
  modelValue: MatchRule
  root?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: MatchRule): void
  (e: 'remove'): void
  (e: 'change'): void
}>()

const rule = computed(() => normalizeMatchRule(props.modelValue))
const isGroup = computed(() => rule.value.type === 'GROUP')
const matcherOptions = computed(() =>
  rule.value.type === 'TEMPLATE_ID' ? TEMPLATE_MATCHER_OPTIONS : PATH_MATCHER_OPTIONS,
)

function updateRule(next: MatchRule) {
  emit('update:modelValue', next)
  emit('change')
}

function updateGroupField<K extends keyof MatchRule>(key: K, value: MatchRule[K]) {
  updateRule({ ...cloneMatchRule(rule.value), [key]: value })
}

function updateChild(index: number, child: MatchRule) {
  const next = cloneMatchRule(rule.value)
  const children = [...(next.children ?? [])]
  children[index] = child
  next.children = children
  updateRule(next)
}

function removeChild(index: number) {
  const next = cloneMatchRule(rule.value)
  next.children = (next.children ?? []).filter((_, idx) => idx !== index)
  updateRule(next)
}

function addPathRule() {
  const next = cloneMatchRule(rule.value)
  next.children = [...(next.children ?? []), makePathMatchRule()]
  updateRule(next)
}

function addTemplateRule() {
  const next = cloneMatchRule(rule.value)
  next.children = [...(next.children ?? []), makeTemplateMatchRule()]
  updateRule(next)
}

function addGroupRule() {
  const next = cloneMatchRule(rule.value)
  next.children = [...(next.children ?? []), makeMatchRuleGroup()]
  updateRule(next)
}

function switchLeafType(type: 'PATH' | 'TEMPLATE_ID') {
  const next =
    type === 'PATH'
      ? makePathMatchRule({ negate: rule.value.negate })
      : makeTemplateMatchRule({ negate: rule.value.negate })
  updateRule(next)
}
</script>

<template>
  <div class=":uno: rounded-md border border-gray-200 bg-white p-3 space-y-3">
    <template v-if="isGroup">
      <div class=":uno: flex flex-wrap items-center gap-2">
        <span class=":uno: text-sm font-medium text-gray-700">
          {{ root ? '根条件组' : '条件组' }}
        </span>
        <select
          :value="rule.operator"
          class=":uno: rounded-md border border-gray-200 px-2 py-1 text-sm focus:border-primary focus:outline-none bg-white"
          @change="
            updateGroupField(
              'operator',
              ($event.target as HTMLSelectElement).value as MatchRule['operator'],
            )
          "
        >
          <option
            v-for="option in MATCH_RULE_GROUP_OPTIONS"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <label class=":uno: inline-flex items-center gap-1 text-xs text-gray-600">
          <input
            :checked="rule.negate"
            type="checkbox"
            @change="updateGroupField('negate', ($event.target as HTMLInputElement).checked)"
          />
          取反
        </label>
        <VButton v-if="!root" size="sm" type="danger" @click="emit('remove')">移除此组</VButton>
      </div>

      <div class=":uno: space-y-2 pl-3 border-l border-gray-200">
        <MatchRuleNodeEditor
          v-for="(child, index) in rule.children ?? []"
          :key="index"
          :model-value="child"
          @change="emit('change')"
          @remove="removeChild(index)"
          @update:model-value="updateChild(index, $event)"
        />

        <div class=":uno: flex flex-wrap gap-2">
          <VButton size="sm" @click="addPathRule">添加路径规则</VButton>
          <VButton size="sm" @click="addTemplateRule">添加模板 ID 规则</VButton>
          <VButton size="sm" type="secondary" @click="addGroupRule">添加条件组</VButton>
        </div>
      </div>
    </template>

    <template v-else>
      <div class=":uno: flex flex-wrap items-center gap-2">
        <select
          :value="rule.type"
          class=":uno: rounded-md border border-gray-200 px-2 py-1 text-sm focus:border-primary focus:outline-none bg-white"
          @change="
            switchLeafType(($event.target as HTMLSelectElement).value as 'PATH' | 'TEMPLATE_ID')
          "
        >
          <option value="PATH">路径匹配</option>
          <option value="TEMPLATE_ID">模板 ID 匹配</option>
        </select>

        <select
          :value="rule.matcher"
          class=":uno: rounded-md border border-gray-200 px-2 py-1 text-sm focus:border-primary focus:outline-none bg-white"
          @change="
            updateGroupField(
              'matcher',
              ($event.target as HTMLSelectElement).value as MatchRule['matcher'],
            )
          "
        >
          <option v-for="option in matcherOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>

        <label class=":uno: inline-flex items-center gap-1 text-xs text-gray-600">
          <input
            :checked="rule.negate"
            type="checkbox"
            @change="updateGroupField('negate', ($event.target as HTMLInputElement).checked)"
          />
          取反
        </label>

        <VButton size="sm" type="danger" @click="emit('remove')">移除此条件</VButton>
      </div>

      <input
        :value="rule.value"
        class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none"
        :placeholder="rule.type === 'PATH' ? '/** 或 ^/posts/.*$' : 'post 或 ^(post|page)$'"
        @input="updateGroupField('value', ($event.target as HTMLInputElement).value)"
      />
    </template>
  </div>
</template>

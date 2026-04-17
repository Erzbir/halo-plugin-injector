<script lang="ts" setup>
import { computed } from 'vue'
import { VButton } from '@halo-dev/components'
import type { MatchRule, MatchRuleMatcher, MatchRuleType } from '@/types'
import {
  makeMatchRuleGroup,
  makePathMatchRule,
  MATCH_RULE_NODE_OPTIONS,
  PATH_MATCHER_OPTIONS,
} from '@/types'

const props = withDefaults(
  defineProps<{
    modelValue: MatchRule
    depth?: number
    canRemove?: boolean
    hasPrevious?: boolean
  }>(),
  {
    depth: 0,
    canRemove: false,
    hasPrevious: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: MatchRule): void
  (e: 'change'): void
  (e: 'remove'): void
}>()

const isGroup = computed(() => props.modelValue.type === 'GROUP')
const showOperatorSelect = computed(() => props.hasPrevious)
const matcherOptions = computed(() => PATH_MATCHER_OPTIONS)
const currentDepth = computed(() => props.depth ?? 0)
const canNegatePathRule = computed(() => !isGroup.value)
const resolvedOperator = computed(() => (props.modelValue.operator === 'OR' ? 'OR' : 'AND'))
const valueError = computed(() => {
  if (isGroup.value) return ''
  const value = props.modelValue.value?.trim() ?? ''
  if (!value) return '路径不能为空'
  if ((props.modelValue.matcher ?? 'PATH_PATTERN') === 'REGEX') {
    try {
      new RegExp(props.modelValue.value ?? '')
    } catch {
      return '正则表达式格式错误'
    }
  }
  return ''
})

function update(next: Partial<MatchRule>) {
  emit('update:modelValue', { ...props.modelValue, ...next })
  emit('change')
}

function updateType(type: MatchRuleType) {
  if (type === props.modelValue.type) return
  const operator = props.modelValue.operator ?? 'AND'
  if (type === 'GROUP') {
    emit('update:modelValue', makeMatchRuleGroup({ operator }))
    emit('change')
    return
  }
  emit('update:modelValue', makePathMatchRule({ operator }))
  emit('change')
}

function updateChild(index: number, nextChild: MatchRule) {
  const children = [...(props.modelValue.children ?? [])]
  children[index] = nextChild
  update({ children })
}

function removeChild(index: number) {
  const children = [...(props.modelValue.children ?? [])]
  children.splice(index, 1)
  update({ children })
}

function addPathChild() {
  const children = [...(props.modelValue.children ?? []), makePathMatchRule({ value: '' })]
  update({ children })
}

function addGroupChild() {
  const children = [...(props.modelValue.children ?? []), makeMatchRuleGroup()]
  update({ children })
}
</script>

<template>
  <div class=":uno: rounded-md border border-gray-200 p-3 space-y-2 bg-white">
    <div class=":uno: flex flex-wrap items-center gap-2">
      <select
        :value="modelValue.type"
        class=":uno: w-auto min-w-max shrink-0 rounded-md border border-gray-200 pl-2 pr-6 py-1 text-xs bg-white"
        @change="updateType(($event.target as HTMLSelectElement).value as MatchRuleType)"
      >
        <option value="GROUP">规则组</option>
        <option value="PATH">路径规则</option>
      </select>

      <select
        v-if="showOperatorSelect"
        :value="resolvedOperator"
        class=":uno: w-auto min-w-max shrink-0 rounded-md border border-gray-200 pl-2 pr-6 py-1 text-xs bg-white"
        @change="
          update({
            operator: ($event.target as HTMLSelectElement).value as MatchRule['operator'],
          })
        "
      >
        <option v-for="o in MATCH_RULE_NODE_OPTIONS" :key="o.value" :value="o.value">
          {{ o.label }}
        </option>
      </select>

      <label
        v-if="canNegatePathRule"
        class=":uno: flex items-center gap-1 text-xs text-gray-600 select-none"
      >
        <input
          type="checkbox"
          class=":uno: rounded border-gray-300"
          :checked="modelValue.operator === 'NOT'"
          @change="
            update({
              operator: ($event.target as HTMLInputElement).checked ? 'NOT' : 'AND',
            })
          "
        />
        取反
      </label>

      <VButton v-if="canRemove" size="xs" type="danger" @click="emit('remove')">删除</VButton>
    </div>

    <template v-if="isGroup">
      <div class=":uno: space-y-2">
        <MatchRuleNodeEditor
          v-for="(child, index) in modelValue.children ?? []"
          :key="index"
          :can-remove="true"
          :depth="currentDepth + 1"
          :has-previous="index > 0"
          :model-value="child"
          @change="emit('change')"
          @remove="removeChild(index)"
          @update:model-value="updateChild(index, $event)"
        />
      </div>

      <div class=":uno: flex gap-2">
        <VButton size="xs" @click="addPathChild">+ 路径</VButton>
        <VButton size="xs" @click="addGroupChild">+ 规则组</VButton>
      </div>
    </template>

    <template v-else>
      <div class=":uno: space-y-1">
        <div class=":uno: flex flex-wrap items-center gap-2">
          <select
            :value="modelValue.matcher ?? 'PATH_PATTERN'"
            class=":uno: w-auto min-w-max shrink-0 rounded-md border border-gray-200 pl-2 pr-6 py-1 text-xs bg-white"
            @change="
              update({
                matcher: ($event.target as HTMLSelectElement).value as MatchRuleMatcher,
              })
            "
          >
            <option v-for="o in matcherOptions" :key="o.value" :value="o.value">
              {{ o.label }}
            </option>
          </select>

          <input
            :placeholder="modelValue.type === 'PATH' ? '/**' : 'post'"
            :value="modelValue.value ?? ''"
            :class="
              valueError
                ? ':uno: match-rule-error-input flex-1 min-w-40 rounded-md border px-2 py-1 text-xs font-mono focus:outline-none'
                : ':uno: flex-1 min-w-40 rounded-md border border-gray-200 px-2 py-1 text-xs font-mono'
            "
            @input="update({ value: ($event.target as HTMLInputElement).value })"
          />
        </div>
        <p v-if="valueError" class=":uno: text-xs text-red-500">{{ valueError }}</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.match-rule-error-input {
  border-color: rgb(248 113 113) !important;
}

.match-rule-error-input:focus {
  border-color: rgb(239 68 68) !important;
  box-shadow: 0 0 0 1px rgb(239 68 68) !important;
}
</style>

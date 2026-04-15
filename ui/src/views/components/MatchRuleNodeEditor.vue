<script lang="ts" setup>
import { computed } from 'vue'
import { VButton } from '@halo-dev/components'
import type { MatchRule, MatchRuleMatcher, MatchRuleType } from '@/types'
import {
  makeMatchRuleGroup,
  makePathMatchRule,
  MATCH_RULE_GROUP_OPTIONS,
  MATCH_RULE_LEAF_OPTIONS,
  PATH_MATCHER_OPTIONS,
} from '@/types'

const props = withDefaults(
  defineProps<{
    modelValue: MatchRule
    depth?: number
    canRemove?: boolean
  }>(),
  {
    depth: 0,
    canRemove: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: MatchRule): void
  (e: 'change'): void
  (e: 'remove'): void
}>()

const isGroup = computed(() => props.modelValue.type === 'GROUP')
const matcherOptions = computed(() => PATH_MATCHER_OPTIONS)
const currentDepth = computed(() => props.depth ?? 0)

function update(next: Partial<MatchRule>) {
  emit('update:modelValue', { ...props.modelValue, ...next })
  emit('change')
}

function updateType(type: MatchRuleType) {
  if (type === props.modelValue.type) return
  if (type === 'GROUP') {
    emit('update:modelValue', makeMatchRuleGroup())
    emit('change')
    return
  }
  emit('update:modelValue', makePathMatchRule())
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
        class=":uno: rounded-md border border-gray-200 px-2 py-1 text-xs bg-white"
        @change="updateType(($event.target as HTMLSelectElement).value as MatchRuleType)"
      >
        <option value="GROUP">规则组</option>
        <option value="PATH">路径规则</option>
      </select>

      <VButton v-if="canRemove" size="xs" type="danger" @click="emit('remove')">删除</VButton>
    </div>

    <template v-if="isGroup">
      <div class=":uno: flex items-center gap-2">
        <span class=":uno: text-xs text-gray-500">组合方式</span>
        <select
          :value="modelValue.operator ?? 'AND'"
          class=":uno: rounded-md border border-gray-200 px-2 py-1 text-xs bg-white"
          @change="
            update({
              operator: ($event.target as HTMLSelectElement).value as MatchRule['operator'],
            })
          "
        >
          <option v-for="o in MATCH_RULE_GROUP_OPTIONS" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>
      </div>

      <div class=":uno: space-y-2">
        <MatchRuleNodeEditor
          v-for="(child, index) in modelValue.children ?? []"
          :key="index"
          :can-remove="true"
          :depth="currentDepth + 1"
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
      <div class=":uno: flex flex-wrap items-center gap-2">
        <select
          :value="modelValue.operator ?? 'AND'"
          class=":uno: rounded-md border border-gray-200 px-2 py-1 text-xs bg-white"
          @change="
            update({
              operator: ($event.target as HTMLSelectElement).value as MatchRule['operator'],
            })
          "
        >
          <option v-for="o in MATCH_RULE_LEAF_OPTIONS" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>

        <select
          :value="modelValue.matcher ?? 'ANT'"
          class=":uno: rounded-md border border-gray-200 px-2 py-1 text-xs bg-white"
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
          class=":uno: flex-1 min-w-40 rounded-md border border-gray-200 px-2 py-1 text-xs font-mono"
          @input="update({ value: ($event.target as HTMLInputElement).value })"
        />
      </div>
    </template>
  </div>
</template>

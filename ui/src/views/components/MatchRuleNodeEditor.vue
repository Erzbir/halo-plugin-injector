<script lang="ts" setup>
import { computed, ref } from 'vue'
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
    sortable?: boolean
  }>(),
  {
    depth: 0,
    canRemove: false,
    hasPrevious: false,
    sortable: false,
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
const resolvedOperator = computed(() => connectorOf(props.modelValue.operator))
const checkedNegation = computed(() => isNegated(props.modelValue.operator))
const draggingChildIndex = ref<number | null>(null)
const dragOverChildIndex = ref<number | null>(null)
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

function isNegated(operator?: MatchRule['operator']) {
  return operator === 'NOT' || operator === 'AND_NOT' || operator === 'OR_NOT'
}

function connectorOf(operator?: MatchRule['operator']): 'AND' | 'OR' {
  return operator === 'OR' || operator === 'OR_NOT' ? 'OR' : 'AND'
}

function combineOperator(
  connector: 'AND' | 'OR',
  negated: boolean,
  hasPrevious: boolean,
): MatchRule['operator'] {
  if (!negated) return connector
  if (connector === 'OR') return 'OR_NOT'
  return hasPrevious ? 'AND_NOT' : 'NOT'
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

function onChildDragStart(index: number, event: DragEvent) {
  event.stopPropagation()
  draggingChildIndex.value = index
  dragOverChildIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(index))
  }
}

function onChildDragOver(index: number, event: DragEvent) {
  if (draggingChildIndex.value === null) return
  event.preventDefault()
  event.stopPropagation()
  dragOverChildIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

function onChildDrop(index: number, event: DragEvent) {
  if (draggingChildIndex.value === null) return
  event.preventDefault()
  event.stopPropagation()
  const fromIndex = draggingChildIndex.value
  const toIndex = index
  draggingChildIndex.value = null
  dragOverChildIndex.value = null
  if (fromIndex === toIndex) return
  const children = [...(props.modelValue.children ?? [])]
  const [moved] = children.splice(fromIndex, 1)
  if (!moved) return
  children.splice(toIndex, 0, moved)
  update({ children })
}

function onChildDragEnd(event: DragEvent) {
  event.stopPropagation()
  draggingChildIndex.value = null
  dragOverChildIndex.value = null
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
            operator: combineOperator(
              ($event.target as HTMLSelectElement).value as 'AND' | 'OR',
              checkedNegation,
              hasPrevious,
            ),
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
          :checked="checkedNegation"
          @change="
            update({
              operator: combineOperator(
                resolvedOperator,
                ($event.target as HTMLInputElement).checked,
                hasPrevious,
              ),
            })
          "
        />
        取反
      </label>

      <VButton v-if="canRemove" size="xs" type="danger" @click="emit('remove')">删除</VButton>
      <span
        v-if="sortable"
        class=":uno: ml-auto inline-flex items-center text-[11px] text-gray-400 select-none cursor-move"
        :draggable="true"
        aria-hidden="true"
      >
        ⋮⋮
      </span>
    </div>

    <template v-if="isGroup">
      <div class=":uno: space-y-2">
        <div
          v-for="(child, index) in modelValue.children ?? []"
          :key="index"
          :class="
            dragOverChildIndex === index && draggingChildIndex !== null
              ? ':uno: rounded-md ring-2 ring-blue-300'
              : ':uno: rounded-md'
          "
          @dragend="onChildDragEnd($event)"
          @dragover="onChildDragOver(index, $event)"
          @dragstart="onChildDragStart(index, $event)"
          @drop="onChildDrop(index, $event)"
        >
          <MatchRuleNodeEditor
            :can-remove="true"
            :depth="currentDepth + 1"
            :has-previous="index > 0"
            :model-value="child"
            :sortable="true"
            @change="emit('change')"
            @remove="removeChild(index)"
            @update:model-value="updateChild(index, $event)"
          />
        </div>
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

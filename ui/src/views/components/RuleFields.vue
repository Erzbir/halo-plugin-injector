<script lang="ts" setup>
import { computed } from 'vue'
import { VButton } from '@halo-dev/components'
import type { InjectionRule } from '@/types'
import { MODE_OPTIONS, POSITION_OPTIONS } from '@/types'
import MatchRuleNodeEditor from './MatchRuleNodeEditor.vue'
import FormField from './FormField.vue'
import SelectDropdown from './SelectDropdown.vue'

const props = withDefaults(
  defineProps<{
    rule: InjectionRule
    dirtyFields?: Partial<Record<keyof InjectionRule, boolean>>
    includeMatchRule?: boolean
  }>(),
  {
    includeMatchRule: true,
  },
)

const emit = defineEmits<{
  (e: 'update:rule', rule: InjectionRule): void
  (e: 'change'): void
  (e: 'revert-field', field: keyof InjectionRule): void
}>()

const needsTarget = computed(() => props.rule.mode === 'ID' || props.rule.mode === 'SELECTOR')

function updateField<K extends keyof InjectionRule>(key: K, value: InjectionRule[K]) {
  emit('update:rule', { ...props.rule, [key]: value })
  emit('change')
}
</script>

<template>
  <FormField label="名称">
    <template #action>
      <VButton
        :class="dirtyFields?.name ? '' : ':uno: invisible pointer-events-none'"
        size="xs"
        @click="emit('revert-field', 'name')"
      >
        撤销修改
      </VButton>
    </template>
    <input
      :value="rule.name"
      class=":uno: w-full rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
      placeholder="不填默认为 ID"
      @change="updateField('name', ($event.target as HTMLInputElement).value)"
    />
  </FormField>

  <FormField label="描述">
    <template #action>
      <VButton
        :class="dirtyFields?.description ? '' : ':uno: invisible pointer-events-none'"
        size="xs"
        @click="emit('revert-field', 'description')"
      >
        撤销修改
      </VButton>
    </template>
    <textarea
      rows="1"
      :value="rule.description"
      class=":uno: w-full min-h-[34px] resize-y rounded-md border border-gray-200 px-3 py-1.5 text-sm focus:border-primary focus:outline-none"
      placeholder="说明此规则的用途"
      @change="updateField('description', ($event.target as HTMLTextAreaElement).value)"
    ></textarea>
  </FormField>

  <FormField label="注入模式" required>
    <template #action>
      <VButton
        :class="dirtyFields?.mode ? '' : ':uno: invisible pointer-events-none'"
        size="xs"
        @click="emit('revert-field', 'mode')"
      >
        撤销修改
      </VButton>
    </template>
    <SelectDropdown
      :model-value="rule.mode"
      :options="MODE_OPTIONS"
      @update:model-value="updateField('mode', $event as InjectionRule['mode'])"
    />
    <p
      v-if="rule.mode === 'SELECTOR' || rule.mode === 'ID'"
      class=":uno: mt-2 rounded-md border border-yellow-200 bg-yellow-50 px-3 py-2 text-xs text-yellow-700"
    >
      使用此模式会带来额外性能开销, 建议仅在必要场景使用
    </p>
  </FormField>

  <template v-if="needsTarget">
    <FormField :label="rule.mode === 'SELECTOR' ? 'CSS 选择器' : '元素 ID'" required>
      <template #action>
        <VButton
          :class="dirtyFields?.match ? '' : ':uno: invisible pointer-events-none'"
          size="xs"
          @click="emit('revert-field', 'match')"
        >
          撤销修改
        </VButton>
      </template>
      <textarea
        rows="1"
        :placeholder="rule.mode === 'SELECTOR' ? 'div[class=content]' : 'main-content'"
        :value="rule.match"
        :class="
          rule.match.trim()
            ? ':uno: w-full min-h-[34px] resize-y rounded-md border border-gray-200 px-3 py-1.5 text-sm font-mono focus:border-primary focus:outline-none'
            : ':uno: w-full min-h-[34px] resize-y rounded-md border border-red-400 px-3 py-1.5 text-sm font-mono focus:border-red-500 focus:outline-none'
        "
        @change="updateField('match', ($event.target as HTMLTextAreaElement).value)"
      ></textarea>
    </FormField>

    <FormField label="插入位置">
      <template #action>
        <VButton
          :class="dirtyFields?.position ? '' : ':uno: invisible pointer-events-none'"
          size="xs"
          @click="emit('revert-field', 'position')"
        >
          撤销修改
        </VButton>
      </template>
      <SelectDropdown
        :model-value="rule.position"
        :options="POSITION_OPTIONS"
        @update:model-value="updateField('position', $event as InjectionRule['position'])"
      />
    </FormField>
  </template>

  <FormField v-if="includeMatchRule" label="匹配规则" required>
    <template #action>
      <VButton
        :class="dirtyFields?.matchRule ? '' : ':uno: invisible pointer-events-none'"
        size="xs"
        @click="emit('revert-field', 'matchRule')"
      >
        撤销修改
      </VButton>
    </template>
    <div class=":uno: max-h-96 overflow-y-auto pr-1">
      <MatchRuleNodeEditor
        :model-value="rule.matchRule"
        @change="emit('change')"
        @update:model-value="updateField('matchRule', $event)"
      />
    </div>
  </FormField>
</template>

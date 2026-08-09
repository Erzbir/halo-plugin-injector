<script lang="ts" setup>
import { computed } from 'vue'
import {
  IconArrowDown,
  VButton,
  vClosePopper,
  VDropdown,
  VDropdownItem,
} from '@halo-dev/components'

type DropdownOption = {
  value: string
  label: string
}

const props = withDefaults(
  defineProps<{
    modelValue: string
    options: DropdownOption[]
    placeholder?: string
    size?: 'xs' | 'sm' | 'md' | 'lg'
    disabled?: boolean
    fullWidth?: boolean
    align?: 'start' | 'end'
    appearance?: 'button' | 'plain'
    prefix?: string
    ariaLabel?: string
  }>(),
  {
    placeholder: '',
    size: 'sm',
    disabled: false,
    fullWidth: true,
    align: 'start',
    appearance: 'button',
    prefix: '',
    ariaLabel: '',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
}>()

const currentLabel = computed(() => {
  const current = props.options.find((option) => option.value === props.modelValue)
  return current?.label ?? props.placeholder
})

const placement = computed(() => (props.align === 'end' ? 'bottom-end' : 'bottom-start'))

function handleSelect(value: string) {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<template>
  <VDropdown :placement="placement">
    <button
      v-if="appearance === 'plain'"
      :aria-label="ariaLabel || undefined"
      :disabled="disabled"
      class=":uno: flex cursor-pointer select-none items-center border-0 bg-transparent p-0 text-sm text-gray-700 hover:text-black disabled:cursor-not-allowed disabled:opacity-60"
      type="button"
    >
      <span class=":uno: mr-0.5 whitespace-nowrap">
        <template v-if="prefix">{{ prefix }}：</template>{{ currentLabel }}
      </span>
      <IconArrowDown class=":uno: h-4 w-4 shrink-0" />
    </button>
    <div v-else :class="fullWidth ? ':uno: w-full' : ':uno: block min-w-0'" class=":uno: relative">
      <VButton
        :class="[fullWidth ? ':uno: !w-full !pr-8' : ':uno: !w-full !pr-5']"
        class=":uno: !justify-start !text-left !overflow-hidden"
        :disabled="disabled"
        :size="size"
      >
        <span
          :class="
            fullWidth
              ? ':uno: block w-full min-w-0 truncate'
              : ':uno: block min-w-0 overflow-hidden text-ellipsis whitespace-nowrap'
          "
          class=":uno: leading-5"
        >
          {{ currentLabel }}
        </span>
      </VButton>
      <span
        :class="fullWidth ? ':uno: right-2' : ':uno: right-1'"
        class=":uno: pointer-events-none absolute top-1/2 -translate-y-1/2 text-gray-400"
      >
        <svg
          :class="size === 'sm' ? ':uno: h-3.5 w-3.5' : ':uno: h-3 w-3'"
          class=":uno: shrink-0"
          fill="none"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M6 9l6 6 6-6"
            stroke="currentColor"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
          />
        </svg>
      </span>
    </div>
    <template #popper>
      <div class=":uno: min-w-40 py-1">
        <VDropdownItem
          v-for="option in options"
          :key="option.value"
          v-close-popper
          :selected="modelValue === option.value"
          @click="handleSelect(option.value)"
        >
          {{ option.label }}
        </VDropdownItem>
      </div>
    </template>
  </VDropdown>
</template>

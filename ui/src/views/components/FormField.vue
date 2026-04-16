<script lang="ts" setup>
defineProps<{
  label: string
  required?: boolean
}>()
</script>

<template>
  <div class=":uno: space-y-1">
    <div class=":uno: flex items-center justify-between gap-2">
      <label class=":uno: text-xs font-medium text-gray-600">
        {{ label }}
        <span v-if="required" class=":uno: text-red-500">*</span>
      </label>
      <div class=":uno: w-20 shrink-0 flex justify-end">
        <slot name="action" />
      </div>
    </div>
    <slot />
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  label?: string
  required?: boolean
}>()
</script>

<template>
  <div class=":uno: space-y-1">
    <div v-if="label || $slots.actions" class=":uno: flex items-center justify-between gap-2">
      <label v-if="label" class=":uno: text-xs font-medium text-gray-600">
        {{ label }}
        <span v-if="required" class=":uno: text-red-500">*</span>
      </label>
      <slot name="actions" />
    </div>
    <slot />
  </div>
</template>

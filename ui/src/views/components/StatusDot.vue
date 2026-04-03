<script lang="ts" setup>
defineProps<{ enabled: boolean }>()
</script>

<template>
  <span
    :class="enabled ? ':uno: bg-primary' : ':uno: bg-gray-500'"
    :title="enabled ? '已启用' : '已停用'"
    class=":uno: shrink-0 w-1.5 h-1.5 rounded-full"
  />
</template>

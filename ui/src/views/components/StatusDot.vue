<script lang="ts" setup>
import { VStatusDot } from '@halo-dev/components'

defineProps<{ enabled: boolean }>()
</script>

<template>
  <span
    :aria-label="enabled ? '已启用' : '已停用'"
    class=":uno: inline-flex shrink-0 items-center gap-1 text-[11px] text-gray-500"
    role="status"
  >
    <VStatusDot :state="enabled ? 'success' : 'default'" aria-hidden="true" />
    <span>{{ enabled ? '已启用' : '已停用' }}</span>
  </span>
</template>

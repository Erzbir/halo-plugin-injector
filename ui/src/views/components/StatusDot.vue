<script lang="ts" setup>
import { VStatusDot } from '@halo-dev/components'

defineProps<{ enabled: boolean }>()
</script>

<template>
  <VStatusDot :state="enabled ? 'success' : 'default'" />
</template>

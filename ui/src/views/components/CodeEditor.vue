<script lang="ts" setup>
import { computed, ref, useTemplateRef } from 'vue'
import { Teleport } from 'vue'
import { VButton } from '@halo-dev/components'

const props = withDefaults(
  defineProps<{
    modelValue: string
    placeholder?: string
    rows?: number
    autofocus?: boolean
    invalid?: boolean
  }>(),
  {
    placeholder: '',
    rows: 10,
    autofocus: false,
    invalid: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
}>()

const fullscreen = ref(false)
const editor = useTemplateRef<HTMLTextAreaElement>('editor')
const fullscreenEditor = useTemplateRef<HTMLTextAreaElement>('fullscreenEditor')
const gutter = useTemplateRef<HTMLDivElement>('gutter')
const fullscreenGutter = useTemplateRef<HTMLDivElement>('fullscreenGutter')

const lineNumbers = computed(() => {
  const lines = Math.max(1, (props.modelValue?.match(/\n/g)?.length ?? 0) + 1)
  return Array.from({ length: lines }, (_, i) => i + 1)
})

function handleInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
}

function handleChange(event: Event) {
  emit('change', (event.target as HTMLTextAreaElement).value)
}

function syncScroll(normal: boolean) {
  if (normal) {
    if (gutter.value && editor.value) gutter.value.scrollTop = editor.value.scrollTop
    return
  }
  if (fullscreenGutter.value && fullscreenEditor.value) {
    fullscreenGutter.value.scrollTop = fullscreenEditor.value.scrollTop
  }
}

function openFullscreen() {
  fullscreen.value = true
}

function closeFullscreen() {
  fullscreen.value = false
}
</script>

<template>
  <div class=":uno: space-y-2">
    <div class=":uno: flex justify-end">
      <VButton size="xs" @click="openFullscreen">全屏</VButton>
    </div>
    <div
      :class="invalid ? ':uno: border-red-400' : ':uno: border-gray-200'"
      class=":uno: w-full rounded-md border bg-white overflow-hidden"
    >
      <div class=":uno: flex">
        <div ref="gutter" class=":uno: w-10 shrink-0 bg-gray-50 border-r border-gray-200 overflow-hidden py-2">
          <div
            v-for="n in lineNumbers"
            :key="n"
            class=":uno: h-5 leading-5 text-right pr-2 text-[11px] text-gray-400 font-mono select-none"
          >
            {{ n }}
          </div>
        </div>
        <textarea
          ref="editor"
          :autofocus="autofocus"
          :placeholder="placeholder"
          :rows="rows"
          :value="modelValue"
          class=":uno: flex-1 min-h-40 border-0 px-3 py-2 text-xs font-mono focus:outline-none leading-5 resize-y"
          spellcheck="false"
          @change="handleChange"
          @input="handleInput"
          @scroll="syncScroll(true)"
        />
      </div>
    </div>
  </div>

  <Teleport to="body">
    <div
      v-if="fullscreen"
      class=":uno: fixed inset-0 z-[10000] bg-black/40 p-4"
      @click.self="closeFullscreen"
    >
      <div
        :class="invalid ? ':uno: border-red-400' : ':uno: border-gray-200'"
        class=":uno: h-full w-full bg-white rounded-lg border shadow-xl flex flex-col"
      >
        <div class=":uno: flex items-center justify-between border-b px-4 py-2">
          <span class=":uno: text-sm font-medium text-gray-700">代码编辑</span>
          <VButton size="xs" @click="closeFullscreen">退出全屏</VButton>
        </div>
        <div class=":uno: flex-1 overflow-hidden">
          <div class=":uno: h-full flex">
            <div
              ref="fullscreenGutter"
              class=":uno: w-12 shrink-0 bg-gray-50 border-r border-gray-200 overflow-hidden py-3"
            >
              <div
                v-for="n in lineNumbers"
                :key="n"
                class=":uno: h-6 leading-6 text-right pr-3 text-xs text-gray-400 font-mono select-none"
              >
                {{ n }}
              </div>
            </div>
            <textarea
              ref="fullscreenEditor"
              :placeholder="placeholder"
              :value="modelValue"
              class=":uno: h-full w-full border-0 px-4 py-3 text-sm font-mono focus:outline-none leading-6 resize-none"
              spellcheck="false"
              @change="handleChange"
              @input="handleInput"
              @scroll="syncScroll(false)"
            />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

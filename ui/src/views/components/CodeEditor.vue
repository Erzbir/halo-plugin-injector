<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useTemplateRef, watch } from 'vue'
import { basicSetup } from 'codemirror'
import { html } from '@codemirror/lang-html'
import { EditorState } from '@codemirror/state'
import { EditorView, placeholder as editorPlaceholder } from '@codemirror/view'
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
const normalHost = useTemplateRef<HTMLDivElement>('normalHost')
const fullscreenHost = useTemplateRef<HTMLDivElement>('fullscreenHost')
const editorHeight = computed(() => `${Math.max(160, props.rows * 20 + 16)}px`)

let view: EditorView | undefined
let updatingFromProps = false
let previousBodyOverflow = ''
let fullscreenTrigger: HTMLElement | null = null

function createEditor() {
  if (!normalHost.value) return
  view = new EditorView({
    parent: normalHost.value,
    state: EditorState.create({
      doc: props.modelValue,
      extensions: [
        basicSetup,
        html({ selfClosingTags: true }),
        EditorState.tabSize.of(2),
        editorPlaceholder(props.placeholder),
        EditorView.lineWrapping,
        EditorView.contentAttributes.of({
          'aria-label': '代码内容',
          'aria-invalid': String(props.invalid),
          'data-placeholder': props.placeholder,
        }),
        EditorView.theme({
          '&': { height: '100%' },
          '.cm-scroller': {
            fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
            fontSize: '12px',
          },
          '.cm-content': { minHeight: '100%' },
          '.cm-gutters': { backgroundColor: '#f9fafb', color: '#9ca3af' },
          '&.cm-focused': { outline: 'none' },
        }),
        EditorView.updateListener.of((update) => {
          if (!update.docChanged || updatingFromProps) return
          emit('update:modelValue', update.state.doc.toString())
        }),
      ],
    }),
  })
  if (props.autofocus) nextTick(() => view?.focus())
}

function moveEditor(host: HTMLDivElement | null) {
  if (view && host) host.append(view.dom)
}

async function openFullscreen() {
  fullscreenTrigger = document.activeElement as HTMLElement | null
  previousBodyOverflow = document.body.style.overflow
  document.body.style.overflow = 'hidden'
  fullscreen.value = true
  await nextTick()
  moveEditor(fullscreenHost.value)
  view?.requestMeasure()
  view?.focus()
}

async function closeFullscreen() {
  moveEditor(normalHost.value)
  fullscreen.value = false
  document.body.style.overflow = previousBodyOverflow
  await nextTick()
  view?.requestMeasure()
  fullscreenTrigger?.focus()
}

function handleFocusOut() {
  emit('change', view?.state.doc.toString() ?? props.modelValue)
}

watch(
  () => props.modelValue,
  (value) => {
    if (!view || value === view.state.doc.toString()) return
    updatingFromProps = true
    view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: value } })
    updatingFromProps = false
  },
)

watch(
  () => props.invalid,
  (invalid) => {
    view?.contentDOM.setAttribute('aria-invalid', String(invalid))
  },
)

onMounted(createEditor)

onBeforeUnmount(() => {
  document.body.style.overflow = previousBodyOverflow
  view?.destroy()
})
</script>

<template>
  <div class=":uno: space-y-2">
    <div class=":uno: flex justify-end">
      <VButton size="xs" @click="openFullscreen">全屏编辑</VButton>
    </div>
    <div
      :class="invalid ? ':uno: border-red-400' : ':uno: border-gray-200'"
      class=":uno: code-editor-frame w-full rounded-md border bg-white overflow-hidden"
      :style="{ height: editorHeight }"
      @focusout="handleFocusOut"
    >
      <div ref="normalHost" class=":uno: code-editor-host h-full" />
    </div>
  </div>

  <Teleport to="body">
    <div
      v-if="fullscreen"
      aria-label="代码编辑"
      aria-modal="true"
      class=":uno: fixed inset-0 z-[10000] bg-black/40 p-4"
      role="dialog"
      @click.self="closeFullscreen"
      @keydown.esc.capture.prevent.stop="closeFullscreen"
    >
      <div
        :class="invalid ? ':uno: border-red-400' : ':uno: border-gray-200'"
        class=":uno: h-full w-full bg-white rounded-lg border shadow-xl flex flex-col overflow-hidden"
      >
        <div class=":uno: flex items-center justify-between border-b px-4 py-2 shrink-0">
          <span class=":uno: text-sm font-medium text-gray-700">代码编辑</span>
          <VButton size="xs" @click="closeFullscreen">退出全屏</VButton>
        </div>
        <div class=":uno: min-h-0 flex-1" @focusout="handleFocusOut">
          <div ref="fullscreenHost" class=":uno: code-editor-host h-full" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.code-editor-host :deep(.cm-editor) {
  height: 100%;
}

.code-editor-host :deep(.cm-scroller) {
  overflow: auto;
}

.code-editor-frame:focus-within {
  border-color: rgb(59 130 246);
  box-shadow: 0 0 0 1px rgb(59 130 246);
}
</style>

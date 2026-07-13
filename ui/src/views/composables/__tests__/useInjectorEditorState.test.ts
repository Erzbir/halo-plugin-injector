import { computed, nextTick, ref } from 'vue'
import { describe, expect, it } from 'vitest'
import { makeRule, makeSnippet } from '@/types'
import { useInjectorEditorState } from '../useInjectorEditorState'

describe('useInjectorEditorState', () => {
  it('uses rule snippet ids even when the legacy snippet relation is stale', async () => {
    const snippets = ref([
      makeSnippet({ id: 's1', ruleIds: [] }),
      makeSnippet({ id: 's2', ruleIds: ['r1'] }),
    ])
    const rules = ref([makeRule({ id: 'r1', snippetIds: ['s1'] })])
    const state = useInjectorEditorState(
      computed(() => snippets.value),
      computed(() => rules.value),
    )

    state.selectedRuleId.value = 'r1'
    await nextTick()

    expect(state.editRuleSnippetIds.value).toEqual(['s1'])
    expect(state.snippetsInRule.value.map((snippet) => snippet.id)).toEqual(['s1'])
  })

  it('tracks and reverts relation changes independently of entity fields', async () => {
    const snippets = ref([makeSnippet({ id: 's1' })])
    const rules = ref([makeRule({ id: 'r1', snippetIds: [] })])
    const state = useInjectorEditorState(
      computed(() => snippets.value),
      computed(() => rules.value),
    )

    state.selectedSnippetId.value = 's1'
    await nextTick()
    state.toggleRuleInSnippetEditor('r1')

    expect(state.snippetDirty.value).toBe(true)
    expect(state.isSnippetFieldDirty('ruleIds')).toBe(true)

    state.revertSnippetField('ruleIds')
    expect(state.snippetDirty.value).toBe(false)
  })
})

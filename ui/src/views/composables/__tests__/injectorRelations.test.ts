import { describe, expect, it, vi } from 'vitest'
import { makeRule } from '@/types'
import {
  detachSnippetsFromRules,
  restoreDetachedSnippetRelations,
  ruleIdsForSnippet,
  syncSnippetRuleRelations,
} from '../injectorRelations'

describe('injectorRelations', () => {
  it('derives reverse relations from canonical rule snippet ids', () => {
    const rules = [
      makeRule({ id: 'r1', snippetIds: ['s1'] }),
      makeRule({ id: 'r2', snippetIds: ['s2', 's1'] }),
      makeRule({ id: 'r3', snippetIds: [] }),
    ]

    expect(ruleIdsForSnippet(rules, 's1')).toEqual(['r1', 'r2'])
  })

  it('updates only rules whose relation changed', async () => {
    const rules = [
      makeRule({ id: 'r1', snippetIds: ['s1'] }),
      makeRule({ id: 'r2', snippetIds: [] }),
      makeRule({ id: 'r3', snippetIds: ['s1'] }),
    ]
    const update = vi.fn(async () => undefined)

    await syncSnippetRuleRelations('s1', ['r1', 'r2'], rules, update)

    expect(update).toHaveBeenCalledTimes(2)
    expect(update).toHaveBeenCalledWith('r2', expect.objectContaining({ snippetIds: ['s1'] }))
    expect(update).toHaveBeenCalledWith('r3', expect.objectContaining({ snippetIds: [] }))
  })

  it('rolls back successful writes when another relation update fails', async () => {
    const r1 = makeRule({ id: 'r1', snippetIds: [] })
    const r2 = makeRule({ id: 'r2', snippetIds: [] })
    const update = vi.fn(async (id: string, rule: ReturnType<typeof makeRule>) => {
      if (id === 'r2' && rule.snippetIds.includes('s1')) throw new Error('failed')
    })

    await expect(syncSnippetRuleRelations('s1', ['r1', 'r2'], [r1, r2], update)).rejects.toThrow(
      'Failed to synchronize',
    )

    expect(update).toHaveBeenCalledWith('r1', r1)
  })

  it('rolls back detach operations when one rule update fails', async () => {
    const r1 = makeRule({ id: 'r1', snippetIds: ['s1'] })
    const r2 = makeRule({ id: 'r2', snippetIds: ['s1'] })
    const update = vi.fn(async (id: string, rule: ReturnType<typeof makeRule>) => {
      if (id === 'r2' && !rule.snippetIds.includes('s1')) throw new Error('failed')
    })

    await expect(detachSnippetsFromRules(['s1'], [r1, r2], update)).rejects.toThrow(
      'Failed to synchronize',
    )

    expect(update).toHaveBeenCalledWith('r1', r1)
  })

  it('restores only snippets whose deletion failed', async () => {
    const rule = makeRule({ id: 'r1', snippetIds: ['kept', 'deleted', 'unrelated'] })
    const update = vi.fn(async () => undefined)

    await restoreDetachedSnippetRelations(['kept', 'deleted'], ['kept'], [rule], update)

    expect(update).toHaveBeenCalledOnce()
    expect(update).toHaveBeenCalledWith(
      'r1',
      expect.objectContaining({ snippetIds: ['kept', 'unrelated'] }),
    )
  })

  it('returns restored rules to the detached state when restoration is incomplete', async () => {
    const r1 = makeRule({ id: 'r1', snippetIds: ['s1'] })
    const r2 = makeRule({ id: 'r2', snippetIds: ['s1'] })
    const update = vi.fn(async (id: string, rule: ReturnType<typeof makeRule>) => {
      if (id === 'r2' && rule.snippetIds.includes('s1')) throw new Error('failed')
    })

    await expect(
      restoreDetachedSnippetRelations(['s1'], ['s1'], [r1, r2], update),
    ).rejects.toThrow('Failed to synchronize')

    expect(update).toHaveBeenCalledWith('r1', expect.objectContaining({ snippetIds: [] }))
  })
})

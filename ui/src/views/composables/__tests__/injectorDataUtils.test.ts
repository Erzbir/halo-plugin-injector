import { describe, expect, it } from 'vitest'
import { apiErrorMessage } from '../injectorDataUtils'

describe('apiErrorMessage', () => {
  it('prefers the response message returned by the API', () => {
    const error = {
      message: 'Request failed',
      response: { data: { message: '名称已经存在' } },
    }

    expect(apiErrorMessage(error, '创建失败')).toBe('创建失败: 名称已经存在')
  })

  it('uses the fallback when no useful error detail is available', () => {
    expect(apiErrorMessage(null, '加载失败')).toBe('加载失败')
  })
})

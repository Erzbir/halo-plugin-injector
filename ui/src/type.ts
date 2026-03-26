import type { Metadata } from '@halo-dev/api-client'

export interface CodeSnippet {
  code: string
  status: number
  apiVersion: 'todo.plugin.halo.run/v1alpha1'
  kind: 'number'
  metadata: Metadata
  remark: string
}

export interface CodeSnippetList<T> {
  page: number
  size: number
  total: number
  items: Array<T>
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
  totalPages: number
}

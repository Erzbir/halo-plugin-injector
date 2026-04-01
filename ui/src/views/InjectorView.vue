<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ruleApi, snippetApi } from '@/apis'
import {
  type ActiveTab,
  type CodeSnippet,
  type InjectionRule,
  type ItemList,
  makeRule,
  makeSnippet,
  MODE_OPTIONS,
  POSITION_OPTIONS,
} from '@/types'

const activeTab = ref<ActiveTab>('snippets')

const snippetsResp = ref<ItemList<CodeSnippet>>({
  first: false,
  hasNext: false,
  hasPrevious: false,
  last: false,
  page: 0,
  size: 0,
  totalPages: 0,
  items: [],
  total: 0,
})
const rulesResp = ref<ItemList<InjectionRule>>({
  first: false,
  hasNext: false,
  hasPrevious: false,
  last: false,
  page: 0,
  size: 0,
  totalPages: 0,
  items: [],
  total: 0,
})

const snippets = computed(() => snippetsResp.value.items)
const rules = computed(() => rulesResp.value.items)

const loading = ref(false)
const saving = ref(false)
const toast = ref<{ msg: string; type: 'success' | 'error' } | null>(null)

const selectedSnippetId = ref<string | null>(null)
const selectedRuleId = ref<string | null>(null)

const editSnippet = ref<CodeSnippet | null>(null)
const editRule = ref<InjectionRule | null>(null)
const editDirty = ref(false)

const showSnippetModal = ref(false)
const showRuleModal = ref(false)
const newSnippet = ref(makeSnippet())
const newRule = ref(makeRule())
const deleteConfirm = ref<{ type: 'snippet' | 'rule'; id: string } | null>(null)

const rulesUsingSnippet = computed(() => {
  if (!selectedSnippetId.value) return []
  return rules.value.filter((r) => r.snippetIds?.includes(selectedSnippetId.value!))
})

const snippetsInRule = computed(() => {
  if (!selectedRuleId.value) return []
  const rule = rules.value.find((r) => r.id === selectedRuleId.value)
  if (!rule?.snippetIds?.length) return []
  return rule.snippetIds
    .map((n) => snippets.value.find((s) => s.id === n))
    .filter((s): s is CodeSnippet => !!s)
})

const editRulePatternsText = computed({
  get() {
    return (editRule.value?.pathPatterns ?? []).map((p) => p.pathPattern).join('\n')
  },
  set(val: string) {
    if (!editRule.value) return
    editRule.value.pathPatterns = val
      .split('\n')
      .map((p) => p.trim())
      .filter(Boolean)
      .map((p) => ({ pathPattern: p }))
    editDirty.value = true
  },
})

const newRulePatternsText = computed({
  get() {
    return (newRule.value.pathPatterns ?? []).map((p) => p.pathPattern).join('\n')
  },
  set(val: string) {
    newRule.value.pathPatterns = val
      .split('\n')
      .map((p) => p.trim())
      .filter(Boolean)
      .map((p) => ({ pathPattern: p }))
  },
})

const editRuleNeedsSelectorOrId = computed(
  () => editRule.value?.mode === 'ID' || editRule.value?.mode === 'SELECTOR',
)
const newRuleNeedsSelectorOrId = computed(
  () => newRule.value.mode === 'ID' || newRule.value.mode === 'SELECTOR',
)

async function fetchAll() {
  loading.value = true
  try {
    const [sr, rr] = await Promise.all([snippetApi.list(), ruleApi.list()])
    snippetsResp.value = sr.data
    rulesResp.value = rr.data
  } catch {
    showToast('加载数据失败', 'error')
  } finally {
    loading.value = false
  }

  if (selectedSnippetId.value) {
    const found = snippetsResp.value.items.find((s) => s.id === selectedSnippetId.value)
    editSnippet.value = found ? JSON.parse(JSON.stringify(found)) : null
  }

  if (selectedRuleId.value) {
    const found = rulesResp.value.items.find((r) => r.id === selectedRuleId.value)
    editRule.value = found ? JSON.parse(JSON.stringify(found)) : null
  }
}

fetchAll()

watch(selectedSnippetId, (id) => {
  const found = snippets.value.find((s) => s.id === id)
  editSnippet.value = found ? JSON.parse(JSON.stringify(found)) : null
  editDirty.value = false
})

watch(selectedRuleId, (id) => {
  const found = rules.value.find((r) => r.id === id)
  editRule.value = found ? JSON.parse(JSON.stringify(found)) : null
  editDirty.value = false
})

function showToast(msg: string, type: 'success' | 'error' = 'success') {
  toast.value = { msg, type }
  setTimeout(() => (toast.value = null), 3000)
}

function openNewSnippetModal() {
  newSnippet.value = makeSnippet()
  showSnippetModal.value = true
}

async function addSnippet() {
  if (!newSnippet.value.code.trim()) {
    showToast('代码内容不能为空', 'error')
    return
  }
  saving.value = true
  try {
    const res = await snippetApi.add(newSnippet.value)
    showSnippetModal.value = false
    await fetchAll()
    selectedSnippetId.value = res.data.id
    showToast('代码块已创建')
  } catch {
    showToast('创建失败', 'error')
  } finally {
    saving.value = false
  }
}

async function saveSnippet() {
  if (!editSnippet.value || !editSnippet.value.code.trim()) {
    showToast('代码内容不能为空', 'error')
    return
  }
  saving.value = true
  try {
    await snippetApi.update(editSnippet.value.id, editSnippet.value)
    await fetchAll()
    editDirty.value = false
    showToast('保存成功')
  } catch {
    showToast('保存失败', 'error')
  } finally {
    saving.value = false
  }
}

function confirmDelete(type: 'snippet' | 'rule', id: string) {
  deleteConfirm.value = { type, id }
}

async function executeDelete() {
  if (!deleteConfirm.value) return
  const { type, id } = deleteConfirm.value
  deleteConfirm.value = null
  try {
    if (type === 'snippet') {
      await snippetApi.delete(id)
      if (selectedSnippetId.value === id) selectedSnippetId.value = null
      showToast('代码块已删除')
    } else {
      await ruleApi.delete(id)
      if (selectedRuleId.value === id) selectedRuleId.value = null
      showToast('规则已删除')
    }
    await fetchAll()
  } catch {
    showToast('删除失败', 'error')
  }
}

function openNewRuleModal() {
  newRule.value = makeRule()
  showRuleModal.value = true
}

async function addRule() {
  const pats = newRule.value.pathPatterns.filter((p) => p.pathPattern.trim())
  if (!pats.length) {
    showToast('至少需要一条路径规则', 'error')
    return
  }
  newRule.value.pathPatterns = pats
  saving.value = true
  try {
    const res = await ruleApi.add(newRule.value)
    showRuleModal.value = false
    await fetchAll()
    selectedRuleId.value = res.data.id
    showToast('规则已创建')
  } catch {
    showToast('创建失败', 'error')
  } finally {
    saving.value = false
  }
}

async function saveRule() {
  if (!editRule.value) return
  const pats = editRule.value.pathPatterns.filter((p) => p.pathPattern.trim())
  if (!pats.length) {
    showToast('至少需要一条路径规则', 'error')
    return
  }
  editRule.value.pathPatterns = pats
  saving.value = true
  try {
    await ruleApi.update(editRule.value.id, editRule.value)
    await fetchAll()
    editDirty.value = false
    showToast('保存成功')
  } catch {
    showToast('保存失败', 'error')
  } finally {
    saving.value = false
  }
}

async function toggleSnippetEnabled(snippet: CodeSnippet) {
  try {
    snippet.enabled = !snippet.enabled
    await snippetApi.update(snippet.id, snippet)
    await fetchAll()
  } catch {
    showToast('操作失败', 'error')
  }
}

async function toggleRuleEnabled(rule: InjectionRule) {
  try {
    rule.enabled = !rule.enabled
    await ruleApi.update(rule.id, rule)
    await fetchAll()
  } catch {
    showToast('操作失败', 'error')
  }
}

function toggleSnippetInEditRule(snippetId: string) {
  if (!editRule.value) return
  const ids = editRule.value.snippetIds ?? []
  const idx = ids.indexOf(snippetId)
  editRule.value.snippetIds = idx === -1 ? [...ids, snippetId] : ids.filter((n) => n !== snippetId)
  editDirty.value = true
}

function toggleSnippetInNewRule(snippetId: string) {
  const ids = newRule.value.snippetIds ?? []
  const idx = ids.indexOf(snippetId)
  newRule.value.snippetIds = idx === -1 ? [...ids, snippetId] : ids.filter((n) => n !== snippetId)
}

function jumpToRule(id: string) {
  activeTab.value = 'rules'
  selectedRuleId.value = id
}
function jumpToSnippet(id: string) {
  activeTab.value = 'snippets'
  selectedSnippetId.value = id
}

function modeLabel(mode: string) {
  return MODE_OPTIONS.find((o) => o.value === mode)?.label ?? mode
}

function patternsPreview(rule: InjectionRule) {
  const pats = rule.pathPatterns ?? []
  if (!pats.length) return '—'
  const shown = pats
    .slice(0, 2)
    .map((p) => p.pathPattern)
    .join('，')
  return pats.length > 2 ? `${shown} 等 ${pats.length} 条` : shown
}

function codePreview(code: string, len = 55) {
  const t = code.replace(/\s+/g, ' ').trim()
  return t.length > len ? t.slice(0, len) + '…' : t
}
</script>

<template>
  <div id="injector-admin">
    <transition name="toast">
      <div v-if="toast" class="toast" :class="`toast--${toast.type}`">
        <span class="toast__icon">{{ toast.type === 'success' ? '✓' : '✕' }}</span>
        {{ toast.msg }}
      </div>
    </transition>

    <header class="topbar">
      <div class="topbar__left">
        <span class="topbar__title">代码注入器</span>
      </div>
      <nav class="topbar__nav">
        <button
          class="nav-tab"
          :class="{ 'nav-tab--active': activeTab === 'snippets' }"
          @click="activeTab = 'snippets'"
        >
          <span class="nav-tab__icon">{ }</span>
          代码块
          <span class="nav-tab__count">{{ snippets.length }}</span>
        </button>
        <button
          class="nav-tab"
          :class="{ 'nav-tab--active': activeTab === 'rules' }"
          @click="activeTab = 'rules'"
        >
          <span class="nav-tab__icon">⚙</span>
          注入规则
          <span class="nav-tab__count">{{ rules.length }}</span>
        </button>
      </nav>
      <div class="topbar__right">
        <button
          v-if="activeTab === 'snippets'"
          class="btn btn--primary btn--sm"
          @click="openNewSnippetModal"
        >
          + 新建代码块
        </button>
        <button v-else class="btn btn--primary btn--sm" @click="openNewRuleModal">
          + 新建规则
        </button>
      </div>
    </header>

    <div class="workspace" :class="{ 'workspace--loading': loading }">
      <aside class="col col--left">
        <template v-if="activeTab === 'snippets'">
          <div v-if="!snippets.length" class="col__empty">
            <p>暂无代码块</p>
            <button class="btn btn--ghost btn--sm" @click="openNewSnippetModal">创建第一个</button>
          </div>
          <ul v-else class="list">
            <li
              v-for="s in snippets"
              :key="s.id"
              class="list__item"
              :class="{ 'list__item--active': selectedSnippetId === s.id }"
              @click="selectedSnippetId = s.id"
            >
              <div class="list__item-header">
                <span class="list__item-name">{{ s.name }}</span>
                <span class="badge" :class="s.enabled ? 'badge--green' : 'badge--gray'">
                  {{ s.enabled ? '启用' : '停用' }}
                </span>
              </div>
              <div v-if="s.description" class="list__item-desc">{{ s.description }}</div>
            </li>
          </ul>
        </template>

        <!-- 规则列表 -->
        <template v-else>
          <div v-if="!rules.length" class="col__empty">
            <p>暂无注入规则</p>
            <button class="btn btn--ghost btn--sm" @click="openNewRuleModal">创建第一个</button>
          </div>
          <ul v-else class="list">
            <li
              v-for="r in rules"
              :key="r.id"
              class="list__item"
              :class="{ 'list__item--active': selectedRuleId === r.id }"
              @click="selectedRuleId = r.id"
            >
              <div class="list__item-header">
                <span class="list__item-name">{{ r.name }}</span>
                <span class="badge" :class="r.enabled ? 'badge--green' : 'badge--gray'">
                  {{ r.enabled ? '启用' : '停用' }}
                </span>
              </div>
              <div v-if="r.description" class="list__item-desc">{{ r.description }}</div>
              <div class="list__item-meta">
                <span class="chip">{{ modeLabel(r.mode) }}</span>
                <span class="list__item-patterns">{{ patternsPreview(r) }}</span>
              </div>
            </li>
          </ul>
        </template>
      </aside>

      <main class="col col--mid">
        <template v-if="activeTab === 'snippets'">
          <div class="col__head">
            <span class="col__head-title">
              {{ editSnippet ? '编辑代码块' : '代码块详情' }}
            </span>
            <div v-if="editSnippet" class="col__head-actions">
              <button
                class="btn"
                :class="editSnippet.enabled ? 'btn--warn' : 'btn--green'"
                :title="editSnippet.enabled ? '禁用' : '启用'"
                @click="toggleSnippetEnabled(editSnippet!)"
              >
                {{ editSnippet.enabled ? '禁用' : '启用' }}
              </button>
              <button
                class="btn btn--danger"
                title="删除"
                @click="confirmDelete('snippet', editSnippet!.id)"
              >
                删除
              </button>
            </div>
          </div>

          <div v-if="!editSnippet" class="col__empty">
            <p>从左侧选择代码块进行编辑</p>
          </div>
          <form v-else class="edit-form" @submit.prevent="saveSnippet">
            <div class="field">
              <label class="field__label">ID</label>
              <input class="field__input field__input--readonly" :value="editSnippet.id" readonly />
            </div>
            <div class="field">
              <label class="field__label">名称</label>
              <input
                class="field__input"
                v-model="editSnippet.name"
                placeholder="不填默认为 ID"
                @input="editDirty = true"
              />
            </div>
            <div class="field">
              <label class="field__label">描述</label>
              <input
                class="field__input"
                v-model="editSnippet.description"
                placeholder="说明此代码块的用途"
                @input="editDirty = true"
              />
            </div>
            <div class="field field--grow">
              <label class="field__label">代码内容</label>
              <textarea
                class="field__textarea field__textarea--code"
                v-model="editSnippet.code"
                placeholder="输入 HTML 代码"
                spellcheck="false"
                @input="editDirty = true"
              />
            </div>
            <div class="edit-form__footer">
              <span v-if="editDirty" class="edit-form__dirty">● 有未保存的修改</span>
              <button class="btn btn--primary" type="submit" :disabled="saving">
                {{ saving ? '保存中…' : '保存' }}
              </button>
            </div>
          </form>
        </template>

        <template v-else>
          <div class="col__head">
            <span class="col__head-title">
              {{ editRule ? '编辑规则' : '规则详情' }}
            </span>
            <div v-if="editRule" class="col__head-actions">
              <button
                class="btn"
                :class="editRule.enabled ? 'btn--warn' : 'btn--green'"
                :title="editRule.enabled ? '禁用' : '启用'"
                @click="toggleRuleEnabled(editRule!)"
              >
                {{ editRule.enabled ? '禁用' : '启用' }}
              </button>
              <button
                class="btn btn--danger"
                title="删除"
                @click="confirmDelete('rule', editRule!.id)"
              >
                删除
              </button>
            </div>
          </div>

          <div v-if="!editRule" class="col__empty">
            <p>从左侧选择规则进行编辑</p>
          </div>
          <form v-else class="edit-form" @submit.prevent="saveRule">
            <div class="field">
              <label class="field__label">ID</label>
              <input class="field__input field__input--readonly" :value="editRule.id" readonly />
            </div>
            <div class="field">
              <label class="field__label">名称</label>
              <input
                class="field__input"
                v-model="editRule.name"
                placeholder="不填默认为 ID"
                @input="editDirty = true"
              />
            </div>
            <div class="field">
              <label class="field__label">描述</label>
              <input
                class="field__input"
                v-model="editRule.description"
                placeholder="说明此规则的用途"
                @input="editDirty = true"
              />
            </div>
            <div class="field">
              <label class="field__label">注入模式</label>
              <select class="field__select" v-model="editRule.mode" @change="editDirty = true">
                <option v-for="o in MODE_OPTIONS" :key="o.value" :value="o.value">
                  {{ o.label }}
                </option>
              </select>
            </div>
            <template v-if="editRuleNeedsSelectorOrId">
              <div class="field" v-if="editRule.mode === 'SELECTOR'">
                <label class="field__label">CSS 选择器</label>
                <input
                  class="field__input field__input--mono"
                  v-model="editRule.match"
                  placeholder="div[class=content]"
                  @input="editDirty = true"
                />
              </div>
              <div class="field" v-if="editRule.mode === 'ID'">
                <label class="field__label">元素 ID</label>
                <input
                  class="field__input field__input--mono"
                  v-model="editRule.match"
                  placeholder="main-content"
                  @input="editDirty = true"
                />
              </div>
              <div class="field">
                <label class="field__label">插入位置</label>
                <select
                  class="field__select"
                  v-model="editRule.position"
                  @change="editDirty = true"
                >
                  <option v-for="o in POSITION_OPTIONS" :key="o.value" :value="o.value">
                    {{ o.label }}
                  </option>
                </select>
              </div>
            </template>
            <div class="field">
              <label class="field__label"
                >路径规则 <span class="field__hint">每行一条, 使用 Ant 模式</span></label
              >
              <textarea
                class="field__textarea field__textarea--mono"
                v-model="editRulePatternsText"
                rows="4"
                placeholder="/**&#10;/blog/**&#10;/about"
                spellcheck="false"
              />
            </div>
            <div class="field">
              <label class="field__label">
                关联代码块
                <span class="field__hint">{{ (editRule.snippetIds ?? []).length }} 个已选</span>
              </label>
              <div class="snippet-picker">
                <div v-if="!snippets.length" class="snippet-picker__empty">
                  暂无代码块，请先创建
                </div>
                <label
                  v-for="s in snippets"
                  :key="s.id"
                  class="snippet-picker__row"
                  :class="{
                    'snippet-picker__row--checked': (editRule.snippetIds ?? []).includes(s.id),
                  }"
                >
                  <input
                    type="checkbox"
                    :checked="(editRule.snippetIds ?? []).includes(s.id)"
                    @change="toggleSnippetInEditRule(s.id)"
                  />
                  <div class="snippet-picker__info">
                    <span class="snippet-picker__name">{{ s.name }}</span>
                    <span v-if="s.description" class="snippet-picker__desc">{{
                      s.description
                    }}</span>
                    <span class="snippet-picker__preview">{{ codePreview(s.code, 40) }}</span>
                  </div>
                  <span
                    class="badge"
                    :class="s.enabled ? 'badge--green' : 'badge--gray'"
                    style="flex-shrink: 0"
                  >
                    {{ s.enabled ? '启用' : '停用' }}
                  </span>
                </label>
              </div>
            </div>
            <div class="edit-form__footer">
              <span v-if="editDirty" class="edit-form__dirty">● 有未保存的修改</span>
              <button class="btn btn--primary" type="submit" :disabled="saving">
                {{ saving ? '保存中…' : '保存' }}
              </button>
            </div>
          </form>
        </template>
      </main>

      <aside class="col col--right">
        <template v-if="activeTab === 'snippets'">
          <div class="col__head">
            <span v-if="!selectedSnippetId" class="col__head-hint">← 选择一个代码块</span>
            <span v-else class="col__head-title">
              被 <em>{{ rulesUsingSnippet.length }}</em> 个规则引用
            </span>
          </div>

          <div v-if="!selectedSnippetId" class="col__empty col__empty--mid">
            <p>选择左侧代码块<br />查看引用它的规则</p>
          </div>
          <div v-else-if="!rulesUsingSnippet.length" class="col__empty col__empty--mid">
            <p>该代码块暂未被任何规则引用</p>
          </div>
          <ul v-else class="rel-list">
            <li
              v-for="r in rulesUsingSnippet"
              :key="r.id"
              class="rel-list__item"
              @click="jumpToRule(r.id)"
            >
              <div class="rel-list__header">
                <span class="rel-list__name">{{ r.name }}</span>
                <span class="badge" :class="r.enabled ? 'badge--green' : 'badge--gray'">
                  {{ r.enabled ? '启用' : '停用' }}
                </span>
              </div>
              <div v-if="r.description" class="rel-list__desc">{{ r.description }}</div>
              <div class="rel-list__meta">
                <span class="chip">{{ modeLabel(r.mode) }}</span>
                <span>{{ patternsPreview(r) }}</span>
              </div>
              <div class="rel-list__jump">点击跳转到规则 →</div>
            </li>
          </ul>
        </template>

        <template v-else>
          <div class="col__head">
            <span v-if="!selectedRuleId" class="col__head-hint">← 选择一个规则</span>
            <span v-else class="col__head-title">
              关联 <em>{{ snippetsInRule.length }}</em> 个代码块
            </span>
          </div>

          <div v-if="!selectedRuleId" class="col__empty col__empty--mid">
            <p>选择左侧规则<br />查看关联的代码块</p>
          </div>
          <div v-else-if="!snippetsInRule.length" class="col__empty col__empty--mid">
            <div class="col__empty-icon">📭</div>
            <p>该规则暂未关联代码块<br />请在右侧编辑面板中添加</p>
          </div>
          <ul v-else class="rel-list">
            <li
              v-for="s in snippetsInRule"
              :key="s.id"
              class="rel-list__item"
              @click="jumpToSnippet(s.id)"
            >
              <div class="rel-list__header">
                <span class="rel-list__name">{{ s.name }}</span>
                <span class="badge" :class="s.enabled ? 'badge--green' : 'badge--gray'">
                  {{ s.enabled ? '启用' : '停用' }}
                </span>
              </div>
              <div v-if="s.description" class="rel-list__desc">{{ s.description }}</div>
              <div class="rel-list__code">{{ codePreview(s.code) }}</div>
              <div class="rel-list__jump">点击跳转到代码块 →</div>
            </li>
          </ul>
        </template>
      </aside>
    </div>

    <teleport to="body">
      <div v-if="showSnippetModal" class="modal-overlay" @click.self="showSnippetModal = false">
        <div class="modal">
          <div class="modal__head">
            <span class="modal__title">新建代码块</span>
            <button class="icon-btn" @click="showSnippetModal = false">✕</button>
          </div>
          <div class="modal__body">
            <div class="field">
              <label class="field__label">名称</label>
              <input class="field__input" v-model="newSnippet.name" placeholder="不填默认为 ID" />
            </div>
            <div class="field">
              <label class="field__label">描述</label>
              <input
                class="field__input"
                v-model="newSnippet.description"
                placeholder="说明此代码块的用途"
              />
            </div>
            <div class="field field--grow">
              <label class="field__label">代码内容 <span class="field__required">*</span></label>
              <textarea
                class="field__textarea field__textarea--code"
                v-model="newSnippet.code"
                placeholder="输入 HTML 代码"
                spellcheck="false"
                rows="14"
                autofocus
              />
            </div>
          </div>
          <div class="modal__foot">
            <button class="btn btn--ghost" @click="showSnippetModal = false">取消</button>
            <button class="btn btn--primary" :disabled="saving" @click="addSnippet">
              {{ saving ? '创建中...' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <teleport to="body">
      <div v-if="showRuleModal" class="modal-overlay" @click.self="showRuleModal = false">
        <div class="modal modal--wide">
          <div class="modal__head">
            <span class="modal__title">新建注入规则</span>
            <button class="icon-btn" @click="showRuleModal = false">✕</button>
          </div>
          <div class="modal__body">
            <div class="modal__cols">
              <div class="modal__col">
                <div class="field">
                  <label class="field__label">名称</label>
                  <input class="field__input" v-model="newRule.name" placeholder="不填默认为 ID" />
                </div>
                <div class="field">
                  <label class="field__label">描述</label>
                  <input
                    class="field__input"
                    v-model="newRule.description"
                    placeholder="说明此规则的用途"
                  />
                </div>
                <div class="field">
                  <label class="field__label"
                    >注入模式 <span class="field__required">*</span></label
                  >
                  <select class="field__select" v-model="newRule.mode">
                    <option v-for="o in MODE_OPTIONS" :key="o.value" :value="o.value">
                      {{ o.label }}
                    </option>
                  </select>
                </div>
                <template v-if="newRuleNeedsSelectorOrId">
                  <div class="field" v-if="newRule.mode === 'SELECTOR'">
                    <label class="field__label"
                      >CSS 选择器 <span class="field__required">*</span></label
                    >
                    <input
                      class="field__input field__input--mono"
                      v-model="newRule.match"
                      placeholder="div[class=content]"
                    />
                  </div>
                  <div class="field" v-if="newRule.mode === 'ID'">
                    <label class="field__label">元素 ID</label>
                    <input
                      class="field__input field__input--mono"
                      v-model="newRule.match"
                      placeholder="main-content"
                      @input="editDirty = true"
                    />
                  </div>
                  <div class="field">
                    <label class="field__label">插入位置</label>
                    <select class="field__select" v-model="newRule.position">
                      <option v-for="o in POSITION_OPTIONS" :key="o.value" :value="o.value">
                        {{ o.label }}
                      </option>
                    </select>
                  </div>
                </template>
                <div class="field">
                  <label class="field__label"
                    >路径规则 <span class="field__required">*</span>
                    <span class="field__hint">每行一条</span></label
                  >
                  <textarea
                    class="field__textarea field__textarea--mono"
                    v-model="newRulePatternsText"
                    rows="4"
                    placeholder="/**&#10;/blog/**"
                    spellcheck="false"
                  />
                </div>
              </div>
              <div class="modal__col">
                <div class="field field--grow">
                  <label class="field__label">
                    关联代码块
                    <span class="field__hint">{{ newRule.snippetIds.length }} 个已选</span>
                  </label>
                  <div class="snippet-picker snippet-picker--tall">
                    <div v-if="!snippets.length" class="snippet-picker__empty">
                      暂无代码块，请先创建
                    </div>
                    <label
                      v-for="s in snippets"
                      :key="s.id"
                      class="snippet-picker__row"
                      :class="{
                        'snippet-picker__row--checked': newRule.snippetIds.includes(s.id),
                      }"
                    >
                      <input
                        type="checkbox"
                        :checked="newRule.snippetIds.includes(s.id)"
                        @change="toggleSnippetInNewRule(s.id)"
                      />
                      <div class="snippet-picker__info">
                        <span class="snippet-picker__name">{{ s.name }}</span>
                        <span v-if="s.description" class="snippet-picker__desc">{{
                          s.description
                        }}</span>
                        <span class="snippet-picker__preview">{{ codePreview(s.code, 40) }}</span>
                      </div>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal__foot">
            <button class="btn btn--ghost" @click="showRuleModal = false">取消</button>
            <button class="btn btn--primary" :disabled="saving" @click="addRule">
              {{ saving ? '创建中…' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <teleport to="body">
      <div v-if="deleteConfirm" class="modal-overlay" @click.self="deleteConfirm = null">
        <div class="modal modal--sm">
          <div class="modal__head">
            <span class="modal__title modal__title--danger">确认删除</span>
            <button class="icon-btn" @click="deleteConfirm = null">✕</button>
          </div>
          <div class="modal__body">
            <p class="modal__confirm-text">
              即将删除{{ deleteConfirm.type === 'snippet' ? '代码块' : '规则' }}
              <strong>{{ deleteConfirm.id }}</strong
              >，此操作不可恢复。
            </p>
            <p v-if="deleteConfirm.type === 'snippet'" class="modal__confirm-warn">
              ⚠ 所有引用此代码块的规则中的关联也将被自动移除。
            </p>
          </div>
          <div class="modal__foot">
            <button class="btn btn--ghost" @click="deleteConfirm = null">取消</button>
            <button class="btn btn--danger" @click="executeDelete">确认删除</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style lang="scss" scoped>
$bg: #f1f5f9;
$surface: #ffffff;
$border: #e2e8f0;
$border-md: #cbd5e1;
$text-1: #0f172a;
$text-2: #475569;
$text-3: #94a3b8;
$accent: #3b82f6;
$accent-bg: #eff6ff;
$accent-dark: #1d4ed8;
$danger: #ef4444;
$danger-bg: #fef2f2;
$green: #16a34a;
$green-bg: #f0fdf4;
$amber: #d97706;
$amber-bg: #fffbeb;
$mono: 'JetBrains Mono', 'Cascadia Code', 'Fira Code', Consolas, monospace;
$radius: 8px;
$radius-sm: 5px;
$radius-lg: 12px;

#injector-admin {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $bg;
  font-size: 13px;
  color: $text-1;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  line-height: 1.5;
}

.toast {
  position: fixed;
  top: 16px;
  right: 20px;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: $radius;
  font-size: 13px;
  font-weight: 500;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);

  &--success {
    background: $green;
    color: #fff;
  }
  &--error {
    background: $danger;
    color: #fff;
  }

  &__icon {
    font-size: 15px;
  }
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.topbar {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 52px;
  background: $surface;
  border-bottom: 1px solid $border;
  flex-shrink: 0;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  &__left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 15px;
    min-width: 0;
  }

  &__logo {
    font-size: 18px;
  }

  &__nav {
    display: flex;
    gap: 2px;
    flex: 1;
  }

  &__right {
    display: flex;
    gap: 8px;
  }
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  border: none;
  background: transparent;
  border-radius: $radius-sm;
  font-size: 13px;
  font-weight: 500;
  color: $text-2;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: $bg;
    color: $text-1;
  }

  &--active {
    background: $accent-bg;
    color: $accent;
  }

  &__icon {
    font-size: 14px;
    opacity: 0.7;
  }

  &__count {
    font-size: 11px;
    font-weight: 600;
    padding: 1px 6px;
    border-radius: 99px;
    background: $border;
    color: $text-2;

    .nav-tab--active & {
      background: $accent;
      color: #fff;
    }
  }
}

.workspace {
  display: grid;
  grid-template-columns: 280px 1fr 340px;
  flex: 1;
  min-height: 0;
  transition: opacity 0.2s;

  &--loading {
    opacity: 0.55;
    pointer-events: none;
  }
}

.col {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;

  &--left {
    background: $surface;
    border-right: 1px solid $border;
  }
  &--mid {
    background: $bg;
    border-right: 1px solid $border;
  }
  &--right {
    background: $surface;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    border-bottom: 1px solid $border;
    background: $surface;
    flex-shrink: 0;
    min-height: 44px;

    &-title {
      font-size: 12px;
      font-weight: 600;
      color: $text-2;
      text-transform: uppercase;
      letter-spacing: 0.04em;

      em {
        font-style: normal;
        color: $accent;
      }
    }

    &-hint {
      font-size: 12px;
      color: $text-3;
    }
    &-actions {
      display: flex;
      gap: 4px;
    }
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    padding: 32px 20px;
    text-align: center;
    color: $text-3;
    gap: 8px;

    &-icon {
      font-size: 36px;
      opacity: 0.4;
    }
    p {
      font-size: 13px;
      line-height: 1.7;
      margin: 0;
    }

    &--mid {
      background: $bg;
    }
  }
}

.list {
  list-style: none;
  padding: 6px;
  overflow-y: auto;
  flex: 1;

  &__item {
    padding: 10px 12px;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: background 0.1s;
    border: 1px solid transparent;
    margin-bottom: 2px;

    &:hover {
      background: $bg;
    }

    &--active {
      background: $accent-bg;
      border-color: rgba($accent, 0.3);
    }

    &-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 6px;
      margin-bottom: 3px;
    }

    &-name {
      font-size: 13px;
      font-weight: 600;
      color: $text-1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
    }

    &-desc {
      font-size: 11px;
      color: $text-2;
      margin-bottom: 3px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &-preview {
      font-size: 11px;
      color: $text-3;
      font-family: $mono;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &-meta {
      display: flex;
      align-items: center;
      gap: 5px;
      font-size: 11px;
      color: $text-2;
      margin-top: 2px;
      overflow: hidden;
    }

    &-patterns {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.rel-list {
  list-style: none;
  padding: 10px;
  overflow-y: auto;
  flex: 1;

  &__item {
    background: $surface;
    border: 1px solid $border;
    border-radius: $radius;
    padding: 12px 14px;
    margin-bottom: 8px;
    cursor: pointer;
    transition:
      border-color 0.15s,
      box-shadow 0.15s;

    &:hover {
      border-color: $accent;
      box-shadow: 0 0 0 3px rgba($accent, 0.08);
    }
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 4px;
  }

  &__name {
    font-size: 13px;
    font-weight: 600;
    color: $text-1;
  }

  &__desc {
    font-size: 11px;
    color: $text-2;
    margin-bottom: 4px;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 11px;
    color: $text-2;
  }

  &__code {
    font-size: 11px;
    font-family: $mono;
    color: $text-3;
    background: $bg;
    padding: 4px 6px;
    border-radius: $radius-sm;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: 4px;
  }

  &__jump {
    font-size: 11px;
    color: $accent;
    margin-top: 6px;
    opacity: 0;
    transition: opacity 0.15s;

    .rel-list__item:hover & {
      opacity: 1;
    }
  }
}

.edit-form {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow-y: auto;
  padding: 14px 16px;
  gap: 14px;

  &__footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 4px;
    flex-shrink: 0;
  }

  &__dirty {
    font-size: 11px;
    color: $amber;
    font-weight: 500;
  }
}

.field {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex-shrink: 0;

  &--grow {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  &__label {
    font-size: 11px;
    font-weight: 600;
    color: $text-2;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    display: flex;
    align-items: center;
    gap: 5px;
  }

  &__hint {
    font-size: 10px;
    color: $text-3;
    text-transform: none;
    letter-spacing: 0;
    font-weight: 400;
  }

  &__required {
    color: $danger;
  }

  &__input {
    padding: 7px 10px;
    border: 1px solid $border;
    border-radius: $radius-sm;
    font-size: 13px;
    color: $text-1;
    background: $surface;
    outline: none;
    transition:
      border-color 0.15s,
      box-shadow 0.15s;

    &:focus {
      border-color: $accent;
      box-shadow: 0 0 0 3px rgba($accent, 0.1);
    }

    &--readonly {
      background: $bg;
      color: $text-2;
      cursor: default;
    }
    &--mono {
      font-family: $mono;
      font-size: 12px;
    }
  }

  &__select {
    padding: 7px 10px;
    border: 1px solid $border;
    border-radius: $radius-sm;
    font-size: 13px;
    color: $text-1;
    background: $surface;
    outline: none;
    cursor: pointer;
    transition: border-color 0.15s;

    &:focus {
      border-color: $accent;
    }
  }

  &__textarea {
    padding: 8px 10px;
    border: 1px solid $border;
    border-radius: $radius-sm;
    font-size: 13px;
    color: $text-1;
    background: $surface;
    outline: none;
    resize: vertical;
    transition: border-color 0.15s;
    line-height: 1.6;

    &:focus {
      border-color: $accent;
      box-shadow: 0 0 0 3px rgba($accent, 0.1);
    }

    &--code {
      flex: 1;
      font-family: $mono;
      font-size: 12px;
      resize: none;
      min-height: 160px;

      &:focus {
        border-color: #388bfd;
        box-shadow: 0 0 0 3px rgba(#388bfd, 0.15);
      }
    }

    &--mono {
      font-family: $mono;
      font-size: 12px;
    }
  }
}

.snippet-picker {
  border: 1px solid $border;
  border-radius: $radius-sm;
  overflow-y: auto;
  max-height: 240px;
  background: $surface;

  &--tall {
    max-height: 100%;
    flex: 1;
  }

  &__empty {
    padding: 16px;
    font-size: 12px;
    color: $text-3;
    text-align: center;
  }

  &__row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 9px 12px;
    border-bottom: 1px solid $border;
    cursor: pointer;
    transition: background 0.1s;

    &:last-child {
      border-bottom: none;
    }
    &:hover {
      background: $bg;
    }

    &--checked {
      background: $accent-bg;
    }

    input[type='checkbox'] {
      width: 14px;
      height: 14px;
      margin-top: 2px;
      flex-shrink: 0;
      accent-color: $accent;
      cursor: pointer;
    }
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: 12px;
    font-weight: 600;
    color: $text-1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__desc {
    font-size: 11px;
    color: $text-2;
  }

  &__preview {
    font-size: 10px;
    color: $text-3;
    font-family: $mono;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 99px;
  font-size: 10px;
  font-weight: 600;
  flex-shrink: 0;

  &--green {
    background: #dcfce7;
    color: #166534;
  }
  &--gray {
    background: #f1f5f9;
    color: $text-2;
  }
}

.chip {
  display: inline-block;
  padding: 2px 6px;
  border-radius: $radius-sm;
  font-size: 10px;
  font-weight: 600;
  background: $bg;
  color: $text-2;
  border: 1px solid $border;
  flex-shrink: 0;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 7px 16px;
  border: 1px solid transparent;
  border-radius: $radius-sm;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &--sm {
    padding: 5px 12px;
    font-size: 12px;
  }

  &--primary {
    background: $accent;
    color: #fff;
    border-color: $accent;
    &:hover:not(:disabled) {
      background: $accent-dark;
      border-color: $accent-dark;
    }
  }

  &--ghost {
    background: transparent;
    color: $text-2;
    border-color: $border;
    &:hover:not(:disabled) {
      border-color: $border-md;
      color: $text-1;
    }
  }

  &--warn {
    background: $amber;
    color: #fff;
    border-color: $amber;
    &:hover:not(:disabled) {
      background: darken($amber, 8%);
    }
  }
  &--green {
    background: $green;
    color: #fff;
    border-color: $green;
    &:hover:not(:disabled) {
      background: darken($green, 8%);
    }
  }

  &--danger {
    background: $danger;
    color: #fff;
    border-color: $danger;
    &:hover:not(:disabled) {
      background: darken($danger, 8%);
    }
  }
}

.icon-btn {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: $radius-sm;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: $text-2;
  transition:
    background 0.1s,
    color 0.1s;

  &:hover {
    background: $bg;
  }
  &--danger {
    &:hover {
      background: $danger-bg;
      color: $danger;
    }
  }
  &--warn {
    &:hover {
      background: $amber-bg;
      color: $amber;
    }
  }
  &--green {
    &:hover {
      background: $green-bg;
      color: $green;
    }
  }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
  padding: 20px;
}

.modal {
  background: $surface;
  border-radius: $radius-lg;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.2);
  width: 540px;
  max-width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &--wide {
    width: 860px;
  }
  &--sm {
    width: 420px;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid $border;
    flex-shrink: 0;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    &--danger {
      color: $danger;
    }
  }

  &__body {
    padding: 20px;
    overflow-y: auto;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  &__cols {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    flex: 1;
    min-height: 0;
  }

  &__col {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  &__foot {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 14px 20px;
    border-top: 1px solid $border;
    flex-shrink: 0;
  }

  &__confirm-text {
    font-size: 14px;
    color: $text-1;
    margin: 0 0 8px;
    line-height: 1.6;
  }

  &__confirm-warn {
    font-size: 12px;
    color: $amber;
    background: $amber-bg;
    border: 1px solid lighten($amber, 30%);
    border-radius: $radius-sm;
    padding: 8px 12px;
    margin: 0;
    line-height: 1.5;
  }
}
</style>

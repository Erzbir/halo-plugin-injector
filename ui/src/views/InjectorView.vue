<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { IconPlug, VButton, VCard, VLoading, VPageHeader } from '@halo-dev/components'

import type { ActiveTab } from '@/types'
import { useInjectorData } from './composables/useInjectorData.ts'
import { rulePreview } from './composables/util.ts'
import { matchRuleChips } from './composables/matchRule.ts'

import ItemListV from './components/ItemListV.vue'
import SnippetEditor from './components/SnippetEditor.vue'
import RuleEditor from './components/RuleEditor.vue'
import RelationPanel from './components/RelationPanel.vue'
import SnippetFormModal from './components/SnippetFormModal.vue'
import RuleFormModal from './components/RuleFormModal.vue'

const activeTab = ref<ActiveTab>('snippets')

const showSnippetModal = ref(false)
const showRuleModal = ref(false)

const {
  loading,
  saving,
  snippets,
  rules,
  selectedSnippetId,
  selectedRuleId,
  editSnippet,
  editSnippetRuleIds,
  editRule,
  editRuleSnippetIds,
  editDirty,
  rulesUsingSnippet,
  snippetsInRule,
  fetchAll,
  addSnippet,
  saveSnippet,
  toggleSnippetEnabled,
  confirmDeleteSnippet,
  toggleRuleInSnippetEditor,
  addRule,
  saveRule,
  toggleRuleEnabled,
  confirmDeleteRule,
  toggleSnippetInRuleEditor,
} = useInjectorData()

onMounted(fetchAll)

async function handleAddSnippet(...args: Parameters<typeof addSnippet>) {
  const id = await addSnippet(...args)
  if (id) showSnippetModal.value = false
}

async function handleAddRule(...args: Parameters<typeof addRule>) {
  const id = await addRule(...args)
  if (id) showRuleModal.value = false
}

function jumpToRule(id: string) {
  activeTab.value = 'rules'
  selectedRuleId.value = id
}

function jumpToSnippet(id: string) {
  activeTab.value = 'snippets'
  selectedSnippetId.value = id
}
</script>

<template>
  <div id="injector-view">
    <SnippetFormModal
      v-if="showSnippetModal"
      :rules="rules"
      :saving="saving"
      @close="showSnippetModal = false"
      @submit="handleAddSnippet"
    />
    <RuleFormModal
      v-if="showRuleModal"
      :saving="saving"
      :snippets="snippets"
      @close="showRuleModal = false"
      @submit="handleAddRule"
    />

    <VPageHeader title="Injector">
      <template #icon><IconPlug /></template>
    </VPageHeader>

    <div class=":uno: m-0 md:m-4">
      <VCard :body-class="['injector-view-card-body']" style="height: calc(100vh - 5.5rem)">
        <div class=":uno: h-full flex divide-x divide-gray-100">
          <div class=":uno: aside h-full flex-none flex flex-col overflow-hidden">
            <div
              class=":uno: sticky top-0 z-10 h-12 flex items-center gap-4 border-b bg-white px-4 shrink-0"
            >
              <button
                v-for="tab in [
                  { key: 'snippets', label: '代码块', count: snippets.length },
                  { key: 'rules', label: '注入规则', count: rules.length },
                ]"
                :key="tab.key"
                :class="
                  activeTab === tab.key
                    ? ':uno: text-primary'
                    : ':uno: text-gray-500 hover:text-gray-800'
                "
                class=":uno: text-sm font-medium transition-colors whitespace-nowrap"
                @click="activeTab = tab.key as ActiveTab"
              >
                {{ tab.label }}
                <span class=":uno: ml-0.5 text-xs">({{ tab.count }})</span>
              </button>
            </div>

            <VLoading v-if="loading" />

            <ItemListV
              v-else-if="activeTab === 'snippets'"
              :items="snippets"
              :selected-id="selectedSnippetId"
              empty-text="暂无代码块"
              @create="showSnippetModal = true"
              @select="selectedSnippetId = $event"
            >
            </ItemListV>

            <ItemListV
              v-else
              :items="rules"
              :selected-id="selectedRuleId"
              :stretch="true"
              empty-text="暂无注入规则"
              @create="showRuleModal = true"
              @select="selectedRuleId = $event"
            >
              <template #meta="{ item: r }">
                <span class=":uno: text-xs text-gray-500">{{ rulePreview(r) }}</span>
                <div class=":uno: flex flex-wrap gap-1 mt-0.5">
                  <span
                    v-for="chip in matchRuleChips(r.matchRule)"
                    :key="chip"
                    class=":uno: text-xs text-gray-500 px-1 py-0.5 rounded border"
                  >
                    {{ chip }}
                  </span>
                </div>
              </template>
            </ItemListV>

            <div class=":uno: h-12 flex items-center justify-center border-t bg-white shrink-0">
              <VButton
                size="sm"
                type="secondary"
                @click="
                  activeTab === 'snippets' ? (showSnippetModal = true) : (showRuleModal = true)
                "
              >
                {{ activeTab === 'snippets' ? '新建代码块' : '新建注入规则' }}
              </VButton>
            </div>
          </div>

          <div class=":uno: main h-full flex-none flex flex-col overflow-hidden">
            <SnippetEditor
              v-if="activeTab === 'snippets'"
              :dirty="editDirty"
              :rules="rules"
              :saving="saving"
              :selected-rule-ids="editSnippetRuleIds"
              :snippet="editSnippet"
              @delete="confirmDeleteSnippet"
              @save="saveSnippet"
              @field-change="editDirty = true"
              @toggle-enabled="toggleSnippetEnabled"
              @toggle-rule="toggleRuleInSnippetEditor"
              @update:snippet="editSnippet = $event"
            />
            <RuleEditor
              v-else
              :dirty="editDirty"
              :rule="editRule"
              :saving="saving"
              :selected-snippet-ids="editRuleSnippetIds"
              :snippets="snippets"
              @delete="confirmDeleteRule"
              @save="saveRule"
              @field-change="editDirty = true"
              @toggle-enabled="toggleRuleEnabled"
              @toggle-snippet="toggleSnippetInRuleEditor"
              @update:rule="editRule = $event"
            />
          </div>

          <div class=":uno: aside h-full flex-none flex flex-col overflow-hidden">
            <RelationPanel
              :mode="activeTab"
              :rules-using-snippet="rulesUsingSnippet"
              :selected-rule-id="selectedRuleId"
              :selected-snippet-id="selectedSnippetId"
              :snippets-in-rule="snippetsInRule"
              @jump-to-rule="jumpToRule"
              @jump-to-snippet="jumpToSnippet"
            />
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

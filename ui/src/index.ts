import { definePlugin } from '@halo-dev/console-shared'
import HomeView from './views/HomeView.vue'
import { IconPlug } from '@halo-dev/components'
import { markRaw } from 'vue'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'ToolsRoot',
      route: {
        path: '/injector',
        name: 'Injector',
        component: HomeView,
        meta: {
          title: '注入插件',
          searchable: true,
          menu: {
            name: '注入插件',
            description: 'HTML 代码注入增强',
            permissions: ['plugin:injector:manage'],
            group: 'tool',
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})

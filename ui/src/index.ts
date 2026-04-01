import { definePlugin } from '@halo-dev/ui-shared'
import InjectorView from './views/InjectorView.vue'
import { IconPlug } from '@halo-dev/components'
import { markRaw } from 'vue'
import './styles/main.scss'
import 'uno.css'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'ToolsRoot',
      route: {
        path: 'injector',
        name: 'Injector',
        component: InjectorView,
        meta: {
          title: 'Injector',
          description: '支持根据规则进行 HTML 代码注入',
          searchable: true,
          permissions: ['plugin:injector:manage'],
          menu: {
            name: 'Injector',
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})

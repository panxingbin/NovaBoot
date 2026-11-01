import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './theme/theme.css'
import 'virtual:uno.css'

import App from './App.vue'
import router from './router'
import i18n from './locales'
import { permission } from './directives/permission'
import { initTheme } from './theme/useTheme'

initTheme()

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(ElementPlus)
app.directive('permission', permission)

app.mount('#app')

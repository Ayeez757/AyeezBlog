import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import '@fortawesome/fontawesome-free/css/all.min.css'
// Twikoo 由各页面通过 @/utils/twikoo 动态加载并 init（避免 UMD 在 Vite 下挂不到 window）

// import './assets/main.css'
import './assets/css/style.css'

const app = createApp(App)

app.use(router)

app.mount('#app')

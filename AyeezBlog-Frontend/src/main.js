import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import '@fortawesome/fontawesome-free/css/all.min.css'
import 'twikoo/dist/twikoo.all.min.js'

// import './assets/main.css'
import './assets/css/style.css'

const app = createApp(App)

app.use(router)

app.mount('#app')

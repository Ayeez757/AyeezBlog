import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// 引入 Element Plus
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css'; // 引入样式

const app = createApp(App);

// 使用 Element Plus
app.use(ElementPlus);
app.use(router);

app.mount('#app');
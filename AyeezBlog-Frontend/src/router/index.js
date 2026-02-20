import { createRouter, createWebHistory } from 'vue-router';

// 引入页面组件
import Home from '@/views/Home.vue';
import About from '@/views/About.vue';
import Archive from '@/views/Archive.vue';
import Links from '@/views/Links.vue';
import FriendsCircle from '@/views/FriendsCircle.vue';
import Comments from '@/views/Comments.vue';
import Logs from '@/views/Logs.vue';


const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/about', name: 'About', component: About },
  { path: '/archive', name: 'Archive', component: Archive },
  { path: '/links', name: 'Links', component: Links },
  { path: '/fc', name: 'FriendsCircle', component: FriendsCircle },
  { path: '/comments', name: 'Comments', component: Comments },
  { path: '/logs', name: 'Logs', component: Logs },

];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
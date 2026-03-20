import { createRouter, createWebHistory } from 'vue-router';

// 引入页面组件
import Home from '@/views/Home.vue';
import About from '@/views/About.vue';
import Archive from '@/views/Archive.vue';
import Links from '@/views/Links.vue';
import FriendsCircle from '@/views/FriendsCircle.vue';
import Comments from '@/views/Comments.vue';
import Logs from '@/views/Logs.vue';
import PostDetail from '@/views/PostDetail.vue';


const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/about', name: 'About', component: About },
  { path: '/archive', name: 'Archive', component: Archive },
  { path: '/links', name: 'Links', component: Links },
  { path: '/fc', name: 'FriendsCircle', component: FriendsCircle },
  { path: '/comments', name: 'Comments', component: Comments },
  { path: '/logs', name: 'Logs', component: Logs },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: PostDetail,
    props: true // 允许通过 props 接收路由参数
  }

];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }

    // 点击首页文章卡片进入详情页时，始终从顶部开始
    if (to.name === 'PostDetail') {
      return { top: 0, left: 0 };
    }

    return { top: 0, left: 0 };
  }
});

export default router;
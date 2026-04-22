import { createRouter, createWebHistory } from 'vue-router';
import { resolveScrollBehaviorWithLenis } from '@/plugins/smoothScroll';

import Home from '@/views/Home.vue';
// 非首页页面拆包（首页加载完后再预取，见 Home.vue）
const About = () => import('@/views/About.vue');
const Archive = () => import('@/views/Archive.vue');
const Links = () => import('@/views/Links.vue');
const FriendsCircle = () => import('@/views/FriendsCircle.vue');
const Comments = () => import('@/views/Comments.vue');
const Logs = () => import('@/views/Logs.vue');
const PostDetail = () => import('@/views/PostDetail.vue');
const Albums = () => import('@/views/Albums.vue');
const AlbumDetail = () => import('@/views/AlbumDetail.vue');
const Talks = () => import('@/views/Talks.vue');
const TalkDetail = () => import('@/views/TalkDetail.vue');


const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/about', name: 'About', component: About },
  { path: '/archive', name: 'Archive', component: Archive },
  { path: '/links', name: 'Links', component: Links },
  { path: '/fc', name: 'FriendsCircle', component: FriendsCircle },
  { path: '/comments', name: 'Comments', component: Comments },
  { path: '/logs', name: 'Logs', component: Logs },
  { path: '/albums', name: 'Albums', component: Albums },
  {
    path: '/albums/:id',
    name: 'AlbumDetail',
    component: AlbumDetail
  },
  { path: '/talks', name: 'Talks', component: Talks },
  {
    path: '/talks/:id',
    name: 'TalkDetail',
    component: TalkDetail,
    props: true
  },
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
  scrollBehavior(_to, _from, savedPosition) {
    return resolveScrollBehaviorWithLenis(savedPosition);
  }
});

export default router;
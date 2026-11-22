import { createRouter, createWebHistory } from 'vue-router';
import { resolveScrollBehaviorWithLenis } from '@/plugins/smoothScroll';
import { updatePageSeo } from '@/utils/seo';
import { ensureAdsenseLoaded } from '@/utils/adsense';

import Home from '@/views/Home.vue';
// 非首页页面拆包（首页加载完后再预取，见 Home.vue）
const About = () => import('@/views/About.vue');
const Privacy = () => import('@/views/Privacy.vue');
const Archive = () => import('@/views/Archive.vue');
const Links = () => import('@/views/Links.vue');
const Comments = () => import('@/views/Comments.vue');
const Logs = () => import('@/views/Logs.vue');
const PostDetail = () => import('@/views/PostDetail.vue');
const Albums = () => import('@/views/Albums.vue');
const AlbumDetail = () => import('@/views/AlbumDetail.vue');
const Talks = () => import('@/views/Talks.vue');
const TalkDetail = () => import('@/views/TalkDetail.vue');
const NotFound = () => import('@/views/NotFound.vue');

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: '',
      description:
        '阿叶 Ayeez 的个人技术博客，记录 Java、Spring Boot、MySQL、Docker、网络原理与项目实战学习笔记。',
      allowAds: true
    }
  },
  {
    path: '/about',
    name: 'About',
    component: About,
    meta: {
      title: '关于我',
      description: '关于阿叶 Ayeez：软件工程在读大学生，技术栈、开源项目与联系方式介绍。',
      allowAds: false
    }
  },
  {
    path: '/privacy',
    name: 'Privacy',
    component: Privacy,
    meta: {
      title: '隐私政策',
      description: '阿叶 Ayeez 的博客隐私政策，说明访问统计、评论、Cookie 与第三方服务使用方式。',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/archive',
    name: 'Archive',
    component: Archive,
    meta: {
      title: '文章归档',
      description: '按时间轴浏览阿叶 Ayeez 博客的全部技术文章与学习笔记。',
      allowAds: false
    }
  },
  {
    path: '/links',
    name: 'Links',
    component: Links,
    meta: {
      title: '友情链接',
      description: '阿叶 Ayeez 博客的友情链接集合。',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/comments',
    name: 'Comments',
    component: Comments,
    meta: {
      title: '留言板',
      description: '阿叶 Ayeez 博客的全站评论与留言互动区。',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/logs',
    name: 'Logs',
    component: Logs,
    meta: {
      title: '更新日志',
      description: '阿叶 Ayeez 博客的版本迭代与功能更新记录。',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/albums',
    name: 'Albums',
    component: Albums,
    meta: {
      title: '相册',
      description: '阿叶 Ayeez 的摄影与生活相册。',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/albums/:id',
    name: 'AlbumDetail',
    component: AlbumDetail,
    meta: {
      title: '相册详情',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/talks',
    name: 'Talks',
    component: Talks,
    meta: {
      title: '说说',
      description: '阿叶 Ayeez 的日常随记与状态动态。',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/talks/:id',
    name: 'TalkDetail',
    component: TalkDetail,
    props: true,
    meta: {
      title: '说说详情',
      robots: 'noindex, follow',
      allowAds: false
    }
  },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: PostDetail,
    props: true, // 允许通过 props 接收路由参数
    meta: {
      title: '文章详情',
      // 文章页是否加载广告由 PostDetail.vue 在确认文章有效且非 editing 状态后决定
      allowAds: false
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: '页面未找到',
      description: '请求的页面不存在。',
      robots: 'noindex, nofollow',
      allowAds: false
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    return resolveScrollBehaviorWithLenis(savedPosition);
  }
});

router.afterEach((to) => {
  updatePageSeo({
    title: to.meta?.title,
    description: to.meta?.description,
    canonicalPath: to.path,
    robots: to.meta?.robots
  });

  if (to.meta?.allowAds) {
    ensureAdsenseLoaded();
  }
});

export default router;
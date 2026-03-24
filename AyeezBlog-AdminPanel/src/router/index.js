import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Article from '@/views/Article.vue'
import Category from '@/views/Category.vue'
import Tag from '@/views/Tag.vue'
import AddArticle from '@/views/AddArticle.vue'
import Contact from '@/views/Contact.vue'
import Login from '@/views/Login.vue'
import Links from '@/views/Links.vue'

const routers = [
  { path: '/login', name: 'Login', component: Login, meta: { requiresAuth: false } },
  { path: '/', name: 'Home', component: Home, meta: { requiresAuth: true } },
  { path: '/article', name: 'Article', component: Article, meta: { requiresAuth: true } },
  { path: '/category', name: 'Category', component: Category, meta: { requiresAuth: true } },
  { path: '/tag', name: 'Tag', component: Tag, meta: { requiresAuth: true } },
  { path: '/links', name: 'Links', component: Links, meta: { requiresAuth: true } },
  { path: '/add-article', name: 'AddArticle', component: AddArticle, meta: { requiresAuth: true } },
  { path: '/edit-article/:id', name: 'EditArticle', component: AddArticle, meta: { requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routers
})

// 路由守卫 - 未登录拦截
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转登录页
    next('/login')
  } else {
    next()
  }
})

export default router

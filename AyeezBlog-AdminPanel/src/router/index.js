import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Article from '@/views/Article.vue'
import Category from '@/views/Category.vue'
import Tag from '@/views/Tag.vue'
import AddArticle from '@/views/AddArticle.vue'
import Contact from '@/views/Contact.vue'


const routers = [
  { path: '/', name: 'Home', component: Home },
  { path: '/article', name: 'Article', component: Article },
  { path: '/category', name: 'Category', component: Category },
  { path: '/tag', name: 'Tag', component: Tag },
   { path: '/add-article', name: 'AddArticle', component: AddArticle },
  // { path: '/contact', name: 'Contact', component: Contact },

];

const router = createRouter({
  history: createWebHistory(),
  routes: routers
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('../components/layout/AppLayout.vue'),
    redirect: '/cities',
    children: [
      {
        path: 'cities',
        name: 'cityManage',
        component: () => import('../views/city/CityManageView.vue'),
        meta: { title: '城市管理' }
      },
      {
        path: 'city-list',
        name: 'cityList',
        component: () => import('../views/city/CityListView.vue'),
        meta: { title: '链表视图' }
      },
      {
        path: 'adjacency',
        name: 'adjacency',
        component: () => import('../views/graph/AdjacencyView.vue'),
        meta: { title: '邻接表' }
      },
      {
        path: 'mst',
        name: 'mst',
        component: () => import('../views/mst/MstView.vue'),
        meta: { title: '最小生成树' }
      },
      {
        path: 'path',
        name: 'path',
        component: () => import('../views/path/PathView.vue'),
        meta: { title: '最短路径' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('../views/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 物流网络系统` : '物流网络系统'
  if (to.meta.public) return true
  if (!getToken()) return { name: 'login' }
  return true
})

export default router

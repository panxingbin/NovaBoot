import { createRouter, createWebHistory } from 'vue-router'
import type { RouteComponent, RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/Layout.vue'
import { useUserStore } from '@/stores/user'
import { i18n } from '@/locales'
import type { MenuTreeNode } from '@/api/types'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { titleKey: 'router.login' },
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { titleKey: 'dashboard.title' },
      },
    ],
  },
]

const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  component: () => import('@/views/notfound/index.vue'),
  meta: { titleKey: 'notfound.title' },
}

const reservedPaths = new Set<string>(['/login', '/dashboard'])

const viewModules = import.meta.glob('../views/**/*.vue') as Record<
  string,
  () => Promise<RouteComponent>
>

function resolveView(component?: string): (() => Promise<RouteComponent>) | undefined {
  if (!component) {
    return undefined
  }
  return viewModules[`../views/${component}.vue`]
}

function buildRoutes(nodes: MenuTreeNode[]): RouteRecordRaw[] {
  const result: RouteRecordRaw[] = []
  for (const node of nodes) {
    if (node.menuType === 'B') {
      continue
    }
    if (node.menuType === 'D') {
      const children = buildRoutes(node.children ?? [])
      result.push({
        path: node.path || `dir-${node.id}`,
        name: `menu-${node.id}`,
        meta: { title: node.name, icon: node.icon },
        children,
        redirect: children[0]?.path,
      })
    } else {
      if (reservedPaths.has(node.path)) {
        continue
      }
      const view = resolveView(node.component)
      if (!view) {
        continue
      }
      result.push({
        path: node.path || `menu-${node.id}`,
        name: `menu-${node.id}`,
        component: view,
        meta: { title: node.name, icon: node.icon },
      })
    }
  }
  return result
}

const router = createRouter({
  history: createWebHistory(),
  routes,
})

let dynamicRouteNames: string[] = []

function collectRouteNames(route: RouteRecordRaw, acc: string[]): void {
  if (typeof route.name === 'string') {
    acc.push(route.name)
  }
  for (const child of route.children ?? []) {
    collectRouteNames(child, acc)
  }
}

export function registerDynamicRoutes(menuTree: MenuTreeNode[]): void {
  for (const name of dynamicRouteNames) {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  }
  dynamicRouteNames = []
  for (const route of buildRoutes(menuTree)) {
    router.addRoute('Layout', route)
    collectRouteNames(route, dynamicRouteNames)
  }
  if (!router.hasRoute('NotFound')) {
    router.addRoute(notFoundRoute)
    dynamicRouteNames.push('NotFound')
  }
}

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    next()
    return
  }

  if (!userStore.token) {
    next({
      path: '/login',
      query: to.fullPath !== '/' ? { redirect: to.fullPath } : undefined,
    })
    return
  }

  if (!userStore.profileLoaded) {
    try {
      await userStore.loadProfile()
      registerDynamicRoutes(userStore.menus)
      next({ path: to.path, query: to.query, hash: to.hash, replace: true })
    } catch {
      userStore.reset()
      next('/login')
    }
    return
  }

  next()
})

/**
 * 當前路由的標題：靜態路由取 i18n 鍵，動態菜單路由用後端返回的名稱
 */
export function pageTitleOf(route: { meta: Record<string, unknown> }): string {
  const titleKey = route.meta.titleKey as string | undefined
  if (titleKey) {
    return i18n.global.t(titleKey)
  }
  return (route.meta.title as string | undefined) ?? ''
}

router.afterEach((to) => {
  const title = pageTitleOf(to)
  document.title = title ? `${title} · NovaBoot` : i18n.global.t('router.defaultTitle')
})

export default router

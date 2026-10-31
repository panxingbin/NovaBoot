import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getProfile, logout as logoutApi } from '@/api/auth'
import { getMenuTree } from '@/api/system'
import { TOKEN_KEY } from '@/api/http'
import type { MenuTreeNode } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  const userId = ref(0)
  const username = ref('')
  const nickname = ref('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<MenuTreeNode[]>([])
  const profileLoaded = ref(false)

  function setToken(value: string): void {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
  }

  function reset(): void {
    token.value = ''
    localStorage.removeItem(TOKEN_KEY)
    userId.value = 0
    username.value = ''
    nickname.value = ''
    roles.value = []
    permissions.value = []
    menus.value = []
    profileLoaded.value = false
  }

  async function loadProfile(): Promise<void> {
    const profile = await getProfile()
    userId.value = profile.user.id
    username.value = profile.user.username
    nickname.value = profile.user.nickname
    roles.value = profile.roles
    permissions.value = profile.permissions
    menus.value = await getMenuTree()
    profileLoaded.value = true
  }

  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } finally {
      reset()
    }
  }

  return {
    token,
    userId,
    username,
    nickname,
    roles,
    permissions,
    menus,
    profileLoaded,
    setToken,
    reset,
    loadProfile,
    logout,
  }
})

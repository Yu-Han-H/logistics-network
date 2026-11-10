import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getToken, setToken, clearToken } from '../utils/auth'
import { getMe, logout as logoutApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const user = ref(null)

  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const roleLabel = computed(() => {
    if (!user.value) return '未登录'
    return user.value.role === 'ADMIN' ? '管理员' : '普通用户'
  })

  function login(data) {
    token.value = data.token
    user.value = data.user
    setToken(data.token)
  }

  async function fetchMe() {
    if (!token.value) return null
    const res = await getMe()
    user.value = res.data
    return user.value
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      // 后端未启动或 token 已失效时，也允许清理本地登录状态
    }
    token.value = ''
    user.value = null
    clearToken()
  }

  return { token, user, isAdmin, roleLabel, login, fetchMe, logout }
})

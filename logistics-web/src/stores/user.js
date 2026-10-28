import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getToken, setToken, clearToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const user = ref(null)

  function login(data) {
    token.value = data.token
    user.value = data.user
    setToken(data.token)
  }

  function logout() {
    token.value = ''
    user.value = null
    clearToken()
  }

  return { token, user, login, logout }
})

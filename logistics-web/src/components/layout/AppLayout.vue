<template>
  <el-container class="app-layout">
    <el-aside width="220px" class="app-aside">
      <AppMenu />
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <span class="app-title">城市间物流网络联通规划系统</span>
        <el-button link type="primary" @click="handleLogout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import AppMenu from './AppMenu.vue'
import { useUserStore } from '../../stores/user'
import { logout as logoutApi } from '../../api/auth'

const router = useRouter()
const userStore = useUserStore()

async function handleLogout() {
  try {
    await logoutApi()
  } catch (e) {
    // 后端未启动时也允许退出本地登录状态
  }
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-aside {
  background: #1f2d3d;
  color: #fff;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
}
</style>

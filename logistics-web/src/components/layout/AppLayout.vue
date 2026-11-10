<template>
  <el-container class="app-layout">
    <el-aside width="220px" class="app-aside">
      <AppMenu />
    </el-aside>
    <el-container class="app-body">
      <el-header class="app-header">
        <span class="app-title">城市间物流网络联通规划系统</span>
        <div class="app-user">
          <template v-if="userStore.user">
            <el-tag size="small" :type="userStore.isAdmin ? 'danger' : 'info'">
              {{ userStore.roleLabel }}
            </el-tag>
            <span class="app-username">{{ userStore.user.username }}</span>
          </template>
          <el-button link type="primary" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppMenu from './AppMenu.vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  // 刷新页面后 Pinia 里的用户会丢，用 token 重新拉一次当前用户
  if (!userStore.user && userStore.token) {
    try {
      await userStore.fetchMe()
    } catch (error) {
      // 401 由 axios 拦截器统一跳登录页
    }
  }
})

async function handleLogout() {
  await userStore.logout()
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
  min-height: 100vh;
}

.app-body {
  min-width: 0;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
}

.app-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-username {
  color: #606266;
  font-size: 13px;
}

.app-main {
  padding: 20px;
  background: #f5f7fa;
}
</style>

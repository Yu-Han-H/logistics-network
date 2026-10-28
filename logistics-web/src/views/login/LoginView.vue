<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">城市间物流网络联通规划系统</div>
      </template>

      <el-form :model="form" label-width="70px" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin 或 user" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-tip">演示账号：admin / admin123，user / user123</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await loginApi({ ...form })
    userStore.login(res.data)
    ElMessage.success('登录成功')
    router.push('/cities')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.login-card {
  width: 420px;
}

.login-title {
  text-align: center;
  font-weight: 600;
}

.login-tip {
  margin: 0;
  color: #909399;
  font-size: 12px;
  text-align: center;
}
</style>

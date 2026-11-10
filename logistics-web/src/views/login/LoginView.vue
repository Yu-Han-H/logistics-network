<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">城市间物流网络联通规划系统</div>
        <div class="login-subtitle">数据结构课程设计 · 第二组</div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="70px"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="admin 或 user"
            autocomplete="username"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        <span>演示账号：</span>
        <el-link type="primary" underline="never" @click="fillAccount('admin')">
          admin / admin123
        </el-link>
        <span class="login-sep">·</span>
        <el-link type="primary" underline="never" @click="fillAccount('user')">
          user / user123
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi } from '../../api/auth'
import { useUserStore } from '../../stores/user'
import { errorMessage } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fillAccount(type) {
  if (type === 'admin') {
    form.username = 'admin'
    form.password = 'admin123'
  } else {
    form.username = 'user'
    form.password = 'user123'
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi({
      username: form.username.trim(),
      password: form.password
    })
    userStore.login(res.data)
    ElMessage.success('登录成功')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const target = redirect.startsWith('/') && redirect !== '/login' ? redirect : '/cities'
    router.replace(target)
  } catch (error) {
    ElMessage.error(errorMessage(error, '登录失败，请检查后端是否启动'))
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
  padding: 20px;
  background: linear-gradient(135deg, #eef4ff 0%, #f5f7fa 45%, #e8f3ff 100%);
}

.login-card {
  width: 420px;
  max-width: 100%;
}

.login-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.login-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.login-button {
  width: 100%;
}

.login-tip {
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.login-sep {
  margin: 0 6px;
  color: #c0c4cc;
}
</style>

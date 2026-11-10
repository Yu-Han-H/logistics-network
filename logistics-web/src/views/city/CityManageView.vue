<template>
  <div>
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            城市管理
            <el-tag type="info" size="small">{{ cities.length }} 个城市</el-tag>
            <el-tag v-if="!userStore.isAdmin" type="warning" size="small">只读</el-tag>
          </div>
          <div class="toolbar">
            <el-button :loading="loading" @click="load">刷新</el-button>
            <el-button v-if="userStore.isAdmin" type="primary" @click="openCreate">
              新增城市
            </el-button>
            <el-button
              v-if="userStore.isAdmin"
              type="danger"
              plain
              :loading="resetting"
              @click="handleReset"
            >
              重置数据
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        class="page-alert"
        @close="error = ''"
      />

      <el-table
        v-loading="loading"
        :data="cities"
        border
        stripe
        empty-text="暂无城市数据，请先新增城市"
      >
        <el-table-column prop="id" label="城市 ID" width="110" />
        <el-table-column prop="name" label="城市名称" min-width="140" />
        <el-table-column prop="longitude" label="经度" width="120">
          <template #default="{ row }">{{ Number(row.longitude).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="latitude" label="纬度" width="120">
          <template #default="{ row }">{{ Number(row.latitude).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column
          v-if="userStore.isAdmin"
          label="操作"
          width="160"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑城市' : '新增城市'"
      width="460px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="城市名称" prop="name">
          <el-input
            v-model="form.name"
            maxlength="20"
            show-word-limit
            placeholder="例如：重庆"
          />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number
            v-model="form.longitude"
            :min="-180"
            :max="180"
            :precision="2"
            :step="0.01"
            controls-position="right"
            class="full-width"
          />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number
            v-model="form.latitude"
            :min="-90"
            :max="90"
            :precision="2"
            :step="0.01"
            controls-position="right"
            class="full-width"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addCity, deleteCity, listCities, updateCity } from '../../api/city'
import { resetData } from '../../api/admin'
import { useUserStore } from '../../stores/user'
import { errorMessage } from '../../utils/format'

const userStore = useUserStore()
const cities = ref([])
const loading = ref(false)
const saving = ref(false)
const resetting = ref(false)
const error = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  longitude: null,
  latitude: null
})

const rules = {
  name: [
    {
      validator: (rule, value, callback) => {
        const name = (value || '').trim()
        if (!name) {
          callback(new Error('请输入城市名称'))
        } else if (name.length > 20) {
          callback(new Error('城市名称不能超过 20 个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  longitude: [
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入经度'))
        } else if (Number(value) < -180 || Number(value) > 180) {
          callback(new Error('经度必须在 -180 到 180 之间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  latitude: [
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入纬度'))
        } else if (Number(value) < -90 || Number(value) > 90) {
          callback(new Error('纬度必须在 -90 到 90 之间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await listCities()
    cities.value = res.data || []
  } catch (requestError) {
    error.value = errorMessage(requestError, '城市列表加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.id = row.id
  form.name = row.name
  form.longitude = row.longitude
  form.latitude = row.latitude
  dialogVisible.value = true
}

function resetForm() {
  form.id = null
  form.name = ''
  form.longitude = null
  form.latitude = null
  formRef.value?.clearValidate()
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      longitude: Number(form.longitude),
      latitude: Number(form.latitude)
    }
    if (form.id) {
      await updateCity(form.id, payload)
      ElMessage.success('城市已更新')
    } else {
      await addCity(payload)
      ElMessage.success('城市已新增')
    }
    dialogVisible.value = false
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除城市「${row.name}」吗？删除后相关边会一起重建。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch (cancel) {
    return
  }

  try {
    await deleteCity(row.id)
    ElMessage.success('城市已删除')
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, '删除失败'))
  }
}

async function handleReset() {
  try {
    await ElMessageBox.confirm(
      '将恢复初始 30 个城市，管理员新增和修改的数据会丢失，确定继续吗？',
      '重置数据',
      { type: 'warning', confirmButtonText: '重置', cancelButtonText: '取消' }
    )
  } catch (cancel) {
    return
  }

  resetting.value = true
  try {
    await resetData()
    ElMessage.success('已恢复初始数据')
    await load()
  } catch (requestError) {
    ElMessage.error(errorMessage(requestError, '重置失败'))
  } finally {
    resetting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.full-width {
  width: 100%;
}
</style>

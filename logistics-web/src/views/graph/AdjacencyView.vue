<template>
  <div>
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            邻接表
            <el-tag type="info" size="small">{{ rows.length }} 个城市</el-tag>
            <el-tag type="info" size="small">{{ recordCount }} 条邻接记录</el-tag>
          </div>
          <div class="toolbar">
            <el-input
              v-model="keyword"
              clearable
              placeholder="搜索城市或邻居"
              style="width: 220px"
            />
            <el-button :loading="loading" @click="load">刷新</el-button>
            <el-button
              type="primary"
              plain
              :disabled="rows.length === 0"
              @click="exportCsv"
            >
              导出 CSV
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

      <div v-if="!loading && rows.length === 0" class="empty-block">
        <el-empty description="暂无邻接表数据，请先添加城市" />
      </div>

      <el-table
        v-else
        v-loading="loading"
        :data="filteredRows"
        row-key="cityId"
        border
        stripe
        :empty-text="keyword ? '没有匹配的城市或邻居' : '暂无邻接表数据'"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="neighbor-panel">
              <el-table :data="row.neighbors" size="small" border>
                <el-table-column prop="toId" label="邻居 ID" width="110" />
                <el-table-column prop="toName" label="邻居城市" min-width="140" />
                <el-table-column label="距离" width="130">
                  <template #default="{ row: neighbor }">
                    {{ formatKm(neighbor.weight) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="cityId" label="城市 ID" width="110" />
        <el-table-column prop="cityName" label="城市名称" width="150" />
        <el-table-column label="度数" width="80" align="center">
          <template #default="{ row }">{{ row.neighbors.length }}</template>
        </el-table-column>
        <el-table-column label="邻居预览" min-width="320">
          <template #default="{ row }">
            <el-tag
              v-for="neighbor in row.neighbors.slice(0, 5)"
              :key="neighbor.toId"
              size="small"
              class="neighbor-tag"
            >
              {{ neighbor.toName }} {{ formatKm(neighbor.weight) }}
            </el-tag>
            <span v-if="row.neighbors.length > 5" class="muted">
              等 {{ row.neighbors.length }} 个邻居
            </span>
          </template>
        </el-table-column>
      </el-table>

      <p class="muted table-note">
        提示：点击每行左侧箭头可展开完整邻居列表。每条无向边在邻接表中存两次，
        所以 30 城完全图应有 435 条边、870 条邻接记录。
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdjacency } from '../../api/graph'
import { downloadCsv, errorMessage, formatKm } from '../../utils/format'

const rows = ref([])
const keyword = ref('')
const loading = ref(false)
const error = ref('')

const filteredRows = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return rows.value
  return rows.value.filter((row) => {
    if (String(row.cityId).includes(key)) return true
    if ((row.cityName || '').toLowerCase().includes(key)) return true
    return (row.neighbors || []).some((neighbor) => (
      String(neighbor.toId).includes(key)
      || (neighbor.toName || '').toLowerCase().includes(key)
    ))
  })
})

const recordCount = computed(() => (
  rows.value.reduce((total, row) => total + (row.neighbors?.length || 0), 0)
))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getAdjacency()
    rows.value = res.data || []
  } catch (requestError) {
    error.value = errorMessage(requestError, '邻接表加载失败')
  } finally {
    loading.value = false
  }
}

function exportCsv() {
  const csvRows = [['城市ID', '城市', '邻居ID', '邻居城市', '距离(km)']]
  for (const row of rows.value) {
    for (const neighbor of row.neighbors || []) {
      csvRows.push([
        row.cityId,
        row.cityName,
        neighbor.toId,
        neighbor.toName,
        Number(neighbor.weight).toFixed(2)
      ])
    }
  }
  downloadCsv('adjacency.csv', csvRows)
}

onMounted(load)
</script>

<style scoped>
.neighbor-panel {
  padding: 10px 20px 14px 48px;
  background: #fafcff;
}

.neighbor-tag {
  margin: 2px 6px 2px 0;
}

.table-note {
  margin: 14px 0 0;
  line-height: 1.7;
}
</style>

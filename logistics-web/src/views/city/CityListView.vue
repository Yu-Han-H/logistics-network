<template>
  <div>
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            链表视图
            <el-tag type="info" size="small">{{ sorted.cities.length }} 个节点</el-tag>
            <el-tag v-if="sorted.reference" type="success" size="small">
              参照：{{ sorted.reference.name }}
            </el-tag>
          </div>
          <div class="toolbar">
            <span class="muted">参照城市</span>
            <el-select
              v-model="referenceId"
              filterable
              clearable
              placeholder="默认北京"
              style="width: 190px"
              @change="handleReferenceChange"
            >
              <el-option
                v-for="city in cities"
                :key="city.id"
                :label="`${city.name} (${city.id})`"
                :value="city.id"
              />
            </el-select>
            <el-button :loading="loading" @click="loadAll">刷新</el-button>
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

      <div v-if="!loading && !sorted.cities.length" class="empty-block">
        <el-empty description="暂无城市数据，请先到城市管理添加城市" />
      </div>

      <template v-else>
        <div class="chain-panel">
          <span class="chain-head">head</span>
          <template v-for="city in sorted.cities" :key="city.id">
            <span class="chain-arrow">→</span>
            <el-tag
              class="chain-node"
              :type="city.id === sorted.reference?.id ? 'success' : 'info'"
              effect="plain"
            >
              {{ city.name }}
              <span class="chain-distance">{{ formatKm(city.distance) }}</span>
            </el-tag>
          </template>
          <span class="chain-arrow">→</span>
          <span class="chain-head">head</span>
        </div>

        <p class="muted chain-note">
          共 {{ sorted.cities.length }} 个节点；首节点的 prev 是尾节点，尾节点的 next
          是首节点，形成双向循环链表。距离按到参照城市的升序排列，距离相同按城市 id
          升序。
        </p>

        <el-table
          v-loading="loading"
          :data="sorted.cities"
          row-key="id"
          border
          stripe
          empty-text="暂无节点"
        >
          <el-table-column prop="id" label="城市 ID" width="110" />
          <el-table-column prop="name" label="城市名称" min-width="140" />
          <el-table-column label="到参照城市距离" width="170">
            <template #default="{ row }">{{ formatKm(row.distance) }}</template>
          </el-table-column>
          <el-table-column label="prev" width="160">
            <template #default="{ row }">
              <span class="table-link">{{ nodeLabel(row.prevId) }}</span>
              <span class="muted"> ({{ row.prevId }})</span>
            </template>
          </el-table-column>
          <el-table-column label="next" width="160">
            <template #default="{ row }">
              <span class="table-link">{{ nodeLabel(row.nextId) }}</span>
              <span class="muted"> ({{ row.nextId }})</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listCities, listSortedCities } from '../../api/city'
import { errorMessage, formatKm } from '../../utils/format'

const cities = ref([])
const referenceId = ref(null)
const sorted = ref({ reference: null, order: 'asc', headId: 0, cities: [] })
const loading = ref(false)
const error = ref('')

const cityNameById = computed(() => {
  const map = new Map()
  for (const city of cities.value) {
    map.set(city.id, city.name)
  }
  return map
})

function nodeLabel(id) {
  return cityNameById.value.get(id) || `#${id}`
}

async function fetchSorted() {
  const res = await listSortedCities(referenceId.value || undefined)
  sorted.value = res.data || { reference: null, order: 'asc', headId: 0, cities: [] }
  if (sorted.value.reference) {
    referenceId.value = sorted.value.reference.id
  }
}

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const cityRes = await listCities()
    cities.value = cityRes.data || []
    await fetchSorted()
  } catch (requestError) {
    error.value = errorMessage(requestError, '链表数据加载失败')
  } finally {
    loading.value = false
  }
}

async function handleReferenceChange() {
  loading.value = true
  error.value = ''
  try {
    await fetchSorted()
  } catch (requestError) {
    error.value = errorMessage(requestError, '按参照城市排序失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.chain-panel {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 16px;
  margin-bottom: 8px;
  overflow-x: auto;
  background: #f8fafc;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
}

.chain-head {
  padding: 4px 9px;
  color: #fff;
  font-size: 12px;
  background: #1f2d3d;
  border-radius: 10px;
}

.chain-arrow {
  color: #a8abb2;
  font-size: 16px;
}

.chain-node {
  height: auto;
  padding: 4px 8px;
}

.chain-distance {
  margin-left: 4px;
  color: #909399;
  font-size: 11px;
}

.chain-note {
  margin: 0 0 14px;
  line-height: 1.7;
}
</style>

<template>
  <div>
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">最短路径查询</div>
        </div>
      </template>

      <el-form :inline="true" class="path-form" @submit.prevent="query">
        <el-form-item label="起点">
          <el-select
            v-model="fromId"
            filterable
            placeholder="选择起点"
            style="width: 190px"
          >
            <el-option
              v-for="city in cities"
              :key="city.id"
              :label="`${city.name} (${city.id})`"
              :value="city.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button circle title="交换起点和终点" @click="swap">⇄</el-button>
        </el-form-item>

        <el-form-item label="终点">
          <el-select
            v-model="toId"
            filterable
            placeholder="选择终点"
            style="width: 190px"
          >
            <el-option
              v-for="city in cities"
              :key="city.id"
              :label="`${city.name} (${city.id})`"
              :value="city.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="模式">
          <el-select v-model="mode" style="width: 160px">
            <el-option label="原图最短路" value="graph" />
            <el-option label="生成树路径" value="tree" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading">
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <p class="muted path-tip">
        默认在原图上跑 Dijkstra。切到“生成树路径”时，路径在 MST
        上唯一，可能比原图最短路更长，只用于对照演示。
      </p>

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        class="page-alert"
        @close="error = ''"
      />
    </el-card>

    <el-card v-if="!result && !loading && !error" class="page-card">
      <el-empty description="选择起点、终点和模式后点击查询" />
    </el-card>

    <el-card v-if="result" class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            查询结果
            <el-tag :type="result.found ? 'success' : 'danger'" size="small">
              {{ result.found ? '找到路径' : '不可达' }}
            </el-tag>
            <el-tag type="info" size="small">
              {{ result.mode === 'tree' ? '生成树路径' : '原图最短路' }}
            </el-tag>
          </div>
          <div class="toolbar">
            <el-switch v-model="showAllEdges" active-text="显示全部边" />
          </div>
        </div>
      </template>

      <el-descriptions :column="4" border class="path-result">
        <el-descriptions-item label="起点">
          {{ result.fromName }} ({{ result.fromId }})
        </el-descriptions-item>
        <el-descriptions-item label="终点">
          {{ result.toName }} ({{ result.toId }})
        </el-descriptions-item>
        <el-descriptions-item label="总代价">
          {{ result.found ? formatKm(result.cost) : '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="边数">
          {{ result.hops }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ formatNanos(result.elapsedNanos) }}
        </el-descriptions-item>
        <el-descriptions-item label="数据版本">
          v{{ result.graphVersion }}
        </el-descriptions-item>
        <el-descriptions-item label="模式">
          {{ result.mode === 'tree' ? '生成树路径' : '原图最短路' }}
        </el-descriptions-item>
        <el-descriptions-item label="路径节点数">
          {{ result.path.length }}
        </el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="!result.found"
        title="从起点到终点不可达。完全图通常不会出现，但代码已处理该情况。"
        type="warning"
        show-icon
        class="page-alert result-alert"
        :closable="false"
      />

      <template v-else>
        <div class="path-chain">
          <template v-for="(node, index) in result.path" :key="`${node.id}-${index}`">
            <span v-if="index > 0" class="path-arrow">→</span>
            <el-tag type="success" effect="plain" class="path-node">
              {{ node.name }}
              <span class="path-node-id">({{ node.id }})</span>
            </el-tag>
          </template>
        </div>

        <el-table :data="result.path" border stripe size="small" class="path-table">
          <el-table-column type="index" label="步骤" width="70" />
          <el-table-column prop="id" label="城市 ID" width="110" />
          <el-table-column prop="name" label="城市名称" />
        </el-table>
      </template>

      <el-card shadow="never" class="graph-card">
        <template #header>
          <div class="card-header">
            <span>路径可视化</span>
            <span class="muted">橙色为最短路径边</span>
          </div>
        </template>
        <NetworkGraph
          :nodes="graphNodes"
          :edges="displayEdges"
          :height="420"
        />
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getGraph } from '../../api/graph'
import { getShortestPath } from '../../api/path'
import { listCities } from '../../api/city'
import NetworkGraph from '../../components/graph/NetworkGraph.vue'
import { errorMessage, formatKm, formatNanos } from '../../utils/format'

const cities = ref([])
const fromId = ref(null)
const toId = ref(null)
const mode = ref('graph')
const result = ref(null)
const graphNodes = ref([])
const graphEdges = ref([])
const showAllEdges = ref(false)
const loading = ref(false)
const error = ref('')

function edgeKey(a, b) {
  const left = String(a)
  const right = String(b)
  return left < right ? `${left}-${right}` : `${right}-${left}`
}

const pathEdges = computed(() => {
  if (!result.value?.found) return []
  const path = result.value.path || []
  const edges = []
  for (let i = 1; i < path.length; i += 1) {
    const from = path[i - 1].id
    const to = path[i].id
    const matched = graphEdges.value.find(
      (edge) => edgeKey(edge.source, edge.target) === edgeKey(from, to)
    )
    edges.push({
      source: String(from),
      target: String(to),
      weight: matched?.weight,
      highlight: true
    })
  }
  return edges
})

const displayEdges = computed(() => {
  if (!showAllEdges.value) return pathEdges.value

  const highlighted = new Set(pathEdges.value.map((edge) => edgeKey(edge.source, edge.target)))
  return graphEdges.value.map((edge) => ({
    ...edge,
    highlight: highlighted.has(edgeKey(edge.source, edge.target))
  }))
})

async function loadCities() {
  try {
    const res = await listCities()
    cities.value = res.data || []
    if (cities.value.length >= 2 && fromId.value === null && toId.value === null) {
      fromId.value = cities.value[0].id
      toId.value = cities.value[1].id
    }
  } catch (requestError) {
    error.value = errorMessage(requestError, '城市列表加载失败')
  }
}

function swap() {
  const temp = fromId.value
  fromId.value = toId.value
  toId.value = temp
}

async function query() {
  if (fromId.value === null || toId.value === null) {
    ElMessage.warning('请选择起点和终点')
    return
  }

  loading.value = true
  error.value = ''
  try {
    const [pathRes, graphRes] = await Promise.all([
      getShortestPath(fromId.value, toId.value, mode.value),
      getGraph()
    ])
    result.value = pathRes.data
    graphNodes.value = graphRes.data?.nodes || []
    graphEdges.value = graphRes.data?.edges || []
    // 不可达时默认把整张图显示出来，避免画布上只有孤立节点
    showAllEdges.value = !pathRes.data.found
  } catch (requestError) {
    error.value = errorMessage(requestError, '最短路径查询失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadCities)
</script>

<style scoped>
.path-form {
  margin-bottom: 4px;
}

.path-tip {
  margin: 0;
  line-height: 1.7;
}

.path-result {
  margin-bottom: 16px;
}

.result-alert {
  margin-top: 16px;
}

.path-chain {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 16px;
  margin-bottom: 16px;
  background: #f8fafc;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
}

.path-arrow {
  color: #a8abb2;
  font-size: 16px;
}

.path-node {
  height: auto;
  padding: 4px 9px;
}

.path-node-id {
  margin-left: 3px;
  color: #67c23a;
  font-size: 11px;
}

.path-table {
  margin-bottom: 16px;
}

.graph-card {
  margin-top: 4px;
}
</style>

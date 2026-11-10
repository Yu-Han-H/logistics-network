<template>
  <div>
    <el-card v-loading="loading" class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            最小生成树
            <el-tag v-if="mst" type="info" size="small">{{ mst.algorithm }}</el-tag>
            <el-tag v-if="mst" type="info" size="small">v{{ mst.graphVersion }}</el-tag>
          </div>
          <div class="toolbar">
            <el-radio-group v-model="algorithm" @change="load">
              <el-radio-button value="prim">Prim</el-radio-button>
              <el-radio-button value="kruskal">Kruskal</el-radio-button>
            </el-radio-group>
            <el-switch v-model="showAllEdges" active-text="显示全部边" />
            <el-button :loading="loading" @click="load">刷新</el-button>
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

      <div v-if="!loading && mst && mst.nodeCount === 0" class="empty-block">
        <el-empty description="暂无城市数据，无法计算最小生成树" />
      </div>

      <template v-else-if="mst">
        <div class="stat-grid">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">节点数</div>
            <div class="stat-value">{{ mst.nodeCount }}</div>
          </el-card>
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">MST 边数</div>
            <div class="stat-value">{{ mst.edgeCount }}</div>
          </el-card>
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">总权值</div>
            <div class="stat-value">{{ formatKm(mst.totalWeight) }}</div>
          </el-card>
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">耗时</div>
            <div class="stat-value is-muted">{{ formatNanos(mst.elapsedNanos) }}</div>
          </el-card>
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">连通性</div>
            <div class="stat-value is-muted">
              <el-tag :type="mst.connected ? 'success' : 'warning'" size="small">
                {{ mst.connected ? '连通' : '不连通（生成森林）' }}
              </el-tag>
            </div>
          </el-card>
        </div>

        <el-alert
          v-if="!mst.connected"
          title="当前图不连通，结果是每个连通分量的最小生成树组成的最小生成森林。"
          type="warning"
          show-icon
          class="page-alert"
          :closable="false"
        />

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-card shadow="never" class="sub-card">
              <template #header>
                <div class="card-header">
                  <span>MST 结构图</span>
                  <span class="muted">橙色为 MST 边</span>
                </div>
              </template>
              <NetworkGraph
                :nodes="graphNodes"
                :edges="displayEdges"
                :height="460"
              />
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-card shadow="never" class="sub-card">
              <template #header>
                <div class="card-header">
                  <span>MST 边列表</span>
                  <span class="muted">{{ mst.edges.length }} 条</span>
                </div>
              </template>
              <el-table
                :data="mst.edges"
                border
                stripe
                size="small"
                max-height="460"
                empty-text="没有选中的边"
              >
                <el-table-column label="边" min-width="180">
                  <template #default="{ row }">
                    {{ row.fromName }} ({{ row.fromId }})
                    —
                    {{ row.toName }} ({{ row.toId }})
                  </template>
                </el-table-column>
                <el-table-column label="权值" width="110" align="right">
                  <template #default="{ row }">{{ formatKm(row.weight) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <p class="muted mst-note">
          总权值 = 所有已取整边权之和，再整体保留 2 位小数；Prim 与 Kruskal
          在同一张图上的总权值必须相等。
        </p>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getGraph } from '../../api/graph'
import { getMst } from '../../api/mst'
import NetworkGraph from '../../components/graph/NetworkGraph.vue'
import { errorMessage, formatKm, formatNanos } from '../../utils/format'

const algorithm = ref('prim')
const showAllEdges = ref(false)
const mst = ref(null)
const graphNodes = ref([])
const graphEdges = ref([])
const loading = ref(false)
const error = ref('')

function edgeKey(a, b) {
  const left = String(a)
  const right = String(b)
  return left < right ? `${left}-${right}` : `${right}-${left}`
}

const mstEdges = computed(() => (
  (mst.value?.edges || []).map((edge) => ({
    source: String(edge.fromId),
    target: String(edge.toId),
    weight: edge.weight,
    highlight: true
  }))
))

const displayEdges = computed(() => {
  if (!showAllEdges.value) return mstEdges.value

  const highlighted = new Set(mstEdges.value.map((edge) => edgeKey(edge.source, edge.target)))
  return graphEdges.value.map((edge) => ({
    ...edge,
    highlight: highlighted.has(edgeKey(edge.source, edge.target))
  }))
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [mstRes, graphRes] = await Promise.all([
      getMst(algorithm.value),
      getGraph()
    ])
    mst.value = mstRes.data
    graphNodes.value = graphRes.data?.nodes || []
    graphEdges.value = graphRes.data?.edges || []
  } catch (requestError) {
    error.value = errorMessage(requestError, '最小生成树加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mst-note {
  margin: 16px 0 0;
  line-height: 1.7;
}
</style>

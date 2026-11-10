<template>
  <div class="network-graph" :style="{ height: normalizedHeight }">
    <div ref="container" class="network-graph__canvas"></div>
    <div v-if="error" class="network-graph__overlay network-graph__overlay--error">
      {{ error }}
    </div>
    <div v-else-if="!nodes.length" class="network-graph__overlay">
      暂无图数据
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Graph } from '@antv/g6'

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  edges: { type: Array, default: () => [] },
  height: { type: [Number, String], default: 460 }
})

const container = ref(null)
const error = ref('')
const normalizedHeight = computed(() => (
  typeof props.height === 'number' ? `${props.height}px` : props.height
))

let graph = null
let fitTimer = null

function toFiniteNumber(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : null
}

/**
 * 有经纬度时按等距圆柱投影把经纬度换算成画布坐标：
 * 经度方向乘 cos(平均纬度)，纬度方向反向（北在上），再等比缩放到 1000。
 * 这样北京在上方、广州在下方、乌鲁木齐在左侧，和真实地理相对位置一致。
 */
function projectNodes(nodes) {
  const geographic = nodes.length > 1 && nodes.every((node) => (
    toFiniteNumber(node.longitude) !== null && toFiniteNumber(node.latitude) !== null
  ))

  if (!geographic) {
    return {
      geographic: false,
      nodes: nodes.map((node) => ({
        id: String(node.id),
        data: { label: node.name || String(node.id) }
      }))
    }
  }

  const longitudes = nodes.map((node) => Number(node.longitude))
  const latitudes = nodes.map((node) => Number(node.latitude))
  const minLongitude = Math.min(...longitudes)
  const maxLatitude = Math.max(...latitudes)
  const middleLatitude = ((Math.min(...latitudes) + maxLatitude) / 2) * Math.PI / 180
  const longitudeScale = Math.cos(middleLatitude)

  const raw = nodes.map((node) => ({
    x: (Number(node.longitude) - minLongitude) * longitudeScale,
    y: maxLatitude - Number(node.latitude)
  }))
  const spanX = Math.max(...raw.map((point) => point.x)) - Math.min(...raw.map((point) => point.x))
  const spanY = Math.max(...raw.map((point) => point.y)) - Math.min(...raw.map((point) => point.y))
  const scale = 1000 / Math.max(spanX, spanY, 1e-6)

  return {
    geographic: true,
    nodes: nodes.map((node, index) => ({
      id: String(node.id),
      data: { label: node.name || String(node.id) },
      style: {
        x: raw[index].x * scale,
        y: raw[index].y * scale
      }
    }))
  }
}

async function renderGraph() {
  error.value = ''
  await nextTick()
  destroyGraph()

  if (!container.value || props.nodes.length === 0) return

  const projected = projectNodes(props.nodes)
  const data = {
    nodes: projected.nodes,
    edges: props.edges.map((edge, index) => ({
      id: edge.id || `${edge.source}-${edge.target}-${index}`,
      source: String(edge.source),
      target: String(edge.target),
      data: {
        weight: edge.weight,
        highlight: Boolean(edge.highlight)
      }
    }))
  }

  try {
    const options = {
      container: container.value,
      autoFit: 'view',
      data,
      node: {
        style: {
          size: 18,
          fill: '#409eff',
          stroke: '#ffffff',
          lineWidth: 2,
          labelText: (datum) => datum.data?.label ?? datum.id,
          labelPlacement: 'bottom',
          labelFill: '#303133',
          labelFontSize: 10
        }
      },
      edge: {
        style: {
          stroke: (datum) => (datum.data?.highlight ? '#e6a23c' : '#d9d9d9'),
          lineWidth: (datum) => (datum.data?.highlight ? 2.5 : 1),
          opacity: (datum) => (datum.data?.highlight ? 1 : 0.35),
          endArrow: false
        }
      },
      behaviors: ['drag-canvas', 'zoom-canvas']
    }

    if (!projected.geographic) {
      options.layout = {
        type: 'force',
        preventOverlap: true,
        nodeSize: 28,
        linkDistance: 90,
        animation: false
      }
    }

    const currentGraph = new Graph(options)
    graph = currentGraph

    // 力导布局结束后再 fit 一次；地理投影模式没有 layout，render 后 fit 即可
    currentGraph.on('afterlayout', () => {
      if (graph === currentGraph) currentGraph.fitView()
    })

    await currentGraph.render()
    if (graph === currentGraph) currentGraph.fitView()

    // 兜底：部分 G6 版本的布局在 render 之后还会微调位置
    fitTimer = window.setTimeout(() => {
      if (graph === currentGraph) currentGraph.fitView()
    }, 800)
  } catch (renderError) {
    error.value = `图渲染失败：${renderError?.message || renderError}`
    destroyGraph()
  }
}

function destroyGraph() {
  if (fitTimer) {
    window.clearTimeout(fitTimer)
    fitTimer = null
  }
  if (graph) {
    graph.destroy()
    graph = null
  }
}

function handleResize() {
  graph?.fitView()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  renderGraph()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  destroyGraph()
})

watch(
  () => [props.nodes, props.edges],
  () => renderGraph(),
  { deep: true }
)
</script>

<style scoped>
.network-graph {
  position: relative;
  width: 100%;
  min-height: 260px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  background: #fbfdff;
}

.network-graph__canvas {
  width: 100%;
  height: 100%;
}

.network-graph__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
  text-align: center;
  background: rgba(251, 253, 255, 0.9);
}

.network-graph__overlay--error {
  color: #f56c6c;
}
</style>

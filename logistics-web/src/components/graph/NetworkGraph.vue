<template>
  <div class="network-graph">
    <p>G6 图将在这里渲染：节点、边、MST 高亮、路径动画。</p>
  </div>
</template>

<script setup>
// TODO: 用 @antv/g6 渲染 /api/graph 返回的 nodes / edges
</script>

<style scoped>
.network-graph {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}
</style>

<template>
  <el-card>
    <template #header>最小生成树</template>
    <p>待实现：Prim / Kruskal 切换，显示总权值和所有边。</p>
    <el-radio-group v-model="algorithm">
      <el-radio-button value="prim">Prim</el-radio-button>
      <el-radio-button value="kruskal">Kruskal</el-radio-button>
    </el-radio-group>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'

const algorithm = ref('prim')
// TODO: 调 src/api/mst.js 的 getMst(algorithm)
</script>

<template>
  <el-card>
    <template #header>最短路径</template>
    <p>待实现：选择起点、终点，显示完整路径、总代价和耗时。</p>
    <el-form inline>
      <el-form-item label="起点">
        <el-input v-model="from" placeholder="城市 id" style="width: 140px" />
      </el-form-item>
      <el-form-item label="终点">
        <el-input v-model="to" placeholder="城市 id" style="width: 140px" />
      </el-form-item>
      <el-form-item label="模式">
        <el-select v-model="mode" style="width: 160px">
          <el-option label="原图最短路" value="graph" />
          <el-option label="生成树路径" value="tree" />
        </el-select>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'

const from = ref('')
const to = ref('')
const mode = ref('graph')
// TODO: 调 src/api/path.js 的 getShortestPath(from, to, mode)
</script>

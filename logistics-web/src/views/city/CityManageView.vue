<template>
  <el-card>
    <template #header>城市管理</template>
    <p>待实现：城市列表、新增、修改、删除。管理员可写，普通用户只读。</p>
    <el-table :data="[]" border>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="name" label="城市" />
      <el-table-column prop="longitude" label="经度" />
      <el-table-column prop="latitude" label="纬度" />
      <el-table-column label="操作" width="160" />
    </el-table>
  </el-card>
</template>

<script setup>
// TODO: 调 src/api/city.js 的 listCities / addCity / updateCity / deleteCity
</script>

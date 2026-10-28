<template>
  <div class="app-menu">
    <div class="app-menu__brand">物流网络</div>
    <el-menu
      :default-active="$route.path"
      router
      background-color="#1f2d3d"
      text-color="#bfcbd9"
      active-text-color="#409eff"
    >
      <el-menu-item index="/cities">城市管理</el-menu-item>
      <el-menu-item index="/city-list">链表视图</el-menu-item>
      <el-menu-item index="/adjacency">邻接表</el-menu-item>
      <el-menu-item index="/mst">最小生成树</el-menu-item>
      <el-menu-item index="/path">最短路径</el-menu-item>
    </el-menu>
  </div>
</template>

<style scoped>
.app-menu__brand {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #2c3e50;
}
</style>

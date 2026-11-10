<template>
  <div class="app-menu">
    <div class="app-menu__brand">
      <span class="brand-main">物流网络</span>
      <span class="brand-sub">联通规划系统</span>
    </div>

    <el-menu
      :default-active="$route.path"
      router
      background-color="#1f2d3d"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      class="app-menu__list"
    >
      <el-menu-item index="/cities">城市管理</el-menu-item>
      <el-menu-item index="/city-list">链表视图</el-menu-item>
      <el-menu-item index="/adjacency">邻接表</el-menu-item>
      <el-menu-item index="/mst">最小生成树</el-menu-item>
      <el-menu-item index="/path">最短路径</el-menu-item>
    </el-menu>

    <div class="app-menu__footer">数据结构课程设计 · 第二组</div>
  </div>
</template>

<style scoped>
.app-menu {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-menu__brand {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 68px;
  padding: 0 20px;
  border-bottom: 1px solid #2c3e50;
}

.brand-main {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.brand-sub {
  color: #8da2b5;
  font-size: 12px;
  line-height: 1.6;
}

.app-menu__list {
  border-right: none;
}

.app-menu__footer {
  margin-top: auto;
  padding: 14px 20px;
  color: #6b7c8d;
  font-size: 11px;
}
</style>

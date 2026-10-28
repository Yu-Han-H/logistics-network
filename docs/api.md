# API 契约

> 详细设计见 [`开发文档-v1.1.md`](开发文档-v1.1.md) 第 7 章。本文件只放“前后端共同遵守”的约定和接口清单，字段变更先改这里。

## 基本约定

- 基础路径：`/api`
- 请求/响应：JSON，UTF-8
- 城市 id：`int`，全系统统一；不要用城市名做外键
- 距离：km，保留 2 位
- 耗时：纳秒（`elapsedNanos`）
- 写操作后 `graphVersion` 加 1

## 统一响应

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

HTTP 状态码与 `code` 保持一致。

## 状态码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / token 无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突（id 或名称重复） |
| 500 | 服务器内部错误 |

## 接口清单

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/api/auth/login` | 公开 | 登录，返回 token |
| GET | `/api/auth/me` | 登录 | 当前用户 |
| POST | `/api/auth/logout` | 登录 | 退出 |
| GET | `/api/cities` | 登录 | 所有城市，按 id 升序 |
| GET | `/api/cities/sorted?reference={id}` | 登录 | 按距离排序，带 prev/next |
| GET | `/api/cities/{id}` | 登录 | 单个城市 |
| POST | `/api/cities` | 管理员 | 新增 |
| PUT | `/api/cities/{id}` | 管理员 | 修改 |
| DELETE | `/api/cities/{id}` | 管理员 | 删除 |
| GET | `/api/graph` | 登录 | 节点和边（G6 用） |
| GET | `/api/graph/adjacency` | 登录 | 邻接表 |
| GET | `/api/mst?algorithm=prim` | 登录 | 最小生成树 |
| GET | `/api/paths/shortest?from={id}&to={id}` | 登录 | 最短路径 |
| POST | `/api/admin/reset` | 管理员 | 重置为 30 城初始数据 |

## 示例

### 登录

```json
{ "username": "admin", "password": "admin123" }
```

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "3f2c8a1e-...",
    "user": { "id": 1, "username": "admin", "role": "ADMIN" }
  }
}
```

### 城市

```json
{ "id": 1000, "name": "北京", "longitude": 116.41, "latitude": 39.90 }
```

### 邻接表

```json
{
  "cityId": 1000,
  "cityName": "北京",
  "neighbors": [
    { "toId": 1001, "toName": "上海", "weight": 1068.39 }
  ]
}
```

### MST

```json
{
  "algorithm": "prim",
  "nodeCount": 30,
  "edgeCount": 29,
  "connected": true,
  "totalWeight": 0.00,
  "edges": [],
  "elapsedNanos": 12500,
  "graphVersion": 3
}
```

### 最短路径

```json
{
  "mode": "graph",
  "found": true,
  "fromId": 1002,
  "fromName": "广州",
  "toId": 1000,
  "toName": "北京",
  "cost": 1890.62,
  "hops": 1,
  "path": [
    { "id": 1002, "name": "广州" },
    { "id": 1000, "name": "北京" }
  ],
  "elapsedNanos": 8900,
  "graphVersion": 3
}
```

> `totalWeight` 在 30 城正式数据算出来后填，不要手写。8 城演示数据见开发文档附录 B。

## 变更记录

| 日期 | 变更 | 影响 |
|---|---|---|
| 2026-10-28 | 初版，按 v1.1 第 7 章整理 | 前端可据此做 mock |

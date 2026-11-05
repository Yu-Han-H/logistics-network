# API 契约

> 2026-11-04 更新：字段、DTO、错误体、认证方式已定稿；后端骨架按本文件搭好。
> 变更规则：先改本文件，再改代码；前后端都按这里写，不要各自发明字段。

## 1. 基本约定

| 项 | 约定 |
|---|---|
| 基础路径 | `/api` |
| 请求/响应 | JSON，UTF-8 |
| 城市标识 | `int` 类型的 `cityId`，全系统统一；不要用城市名做外键 |
| 距离 | km，保留 2 位小数 |
| 耗时 | 纳秒，字段名 `elapsedNanos` |
| 数据版本 | `graphVersion`，任何写操作后加 1 |
| 认证 | `Authorization: Bearer <token>` |
| 时间 | 不返回时间戳，前端不依赖 |

## 2. 统一响应

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败时：

```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": null,
  "errors": [
    { "field": "longitude", "message": "经度必须在 -180 到 180 之间" }
  ]
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | int | 业务状态码，和 HTTP 状态码一致 |
| `message` | string | 提示信息 |
| `data` | object / array / null | 业务数据 |
| `errors` | array，可选 | 字段校验错误；没有时不返回 |

**HTTP 状态码 = `code`。** 不要出现 HTTP 200 + body `code=500`。

## 3. 状态码

| code | 含义 | 典型场景 |
|---|---|---|
| 200 | 成功 | 查询、增删改成功 |
| 400 | 参数错误 | 经纬度越界、`algorithm` 不是 prim/kruskal、`mode` 不是 graph/tree |
| 401 | 未登录 / token 无效 | 没带 token、token 已退出 |
| 403 | 无权限 | 普通用户调增删改接口 |
| 404 | 资源不存在 | 城市 id 不存在 |
| 409 | 冲突 | 城市 id 或名称重复 |
| 500 | 服务器内部错误 | 未捕获异常 |

## 4. 认证

### 4.1 账号

课程演示，预置两个账号，密码明文，不用于真实系统：

| 用户名 | 密码 | 角色 |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `user` | `user123` | `USER` |

### 4.2 登录

```http
POST /api/auth/login
Content-Type: application/json

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

- token 是 UUID 字符串，存在后端内存里；服务重启后失效。
- 前端把 token 放 `sessionStorage`。
- 之后每个请求带：`Authorization: Bearer <token>`。

### 4.3 当前用户 / 退出

```http
GET /api/auth/me
Authorization: Bearer <token>
```

```json
{
  "code": 200,
  "message": "success",
  "data": { "id": 1, "username": "admin", "role": "ADMIN" }
}
```

```http
POST /api/auth/logout
Authorization: Bearer <token>
```

退出成功返回 `data: null`。

## 5. DTO

### 5.1 CityDTO

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 城市 id |
| `name` | string | 城市名 |
| `longitude` | double | 经度 |
| `latitude` | double | 纬度 |

```json
{ "id": 1000, "name": "北京", "longitude": 116.41, "latitude": 39.90 }
```

> 城市对象**不带** `prevId`/`nextId`。链表指针只在 `/api/cities/sorted` 返回。

### 5.2 CityRequest（新增/修改）

| 字段 | 类型 | 约束 |
|---|---|---|
| `name` | string | 必填，1-20 字符，不能重名 |
| `longitude` | double | 必填，-180 到 180 |
| `latitude` | double | 必填，-90 到 90 |

```json
{ "name": "重庆", "longitude": 106.55, "latitude": 29.56 }
```

`id` 由后端生成，前端不传。

### 5.3 SortedCitiesDTO（排序链表）

| 字段 | 类型 | 说明 |
|---|---|---|
| `reference` | object | 参照城市 `{id, name}` |
| `order` | string | 固定 `asc` |
| `headId` | int | 链表第一个城市 id |
| `cities` | array | 按距离升序的节点 |

`cities` 的每一项：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 城市 id |
| `name` | string | 城市名 |
| `distance` | double | 到参照城市的距离，km |
| `prevId` | int | 前一个城市 id |
| `nextId` | int | 后一个城市 id |

```json
{
  "reference": { "id": 1000, "name": "北京" },
  "order": "asc",
  "headId": 1000,
  "cities": [
    { "id": 1000, "name": "北京", "distance": 0.00, "prevId": 1007, "nextId": 1007 },
    { "id": 1007, "name": "沈阳", "distance": 627.82, "prevId": 1000, "nextId": 1000 }
  ]
}
```

> 最后一个城市的 `nextId` 回到 `headId`，表示循环。上面只列两个城市，实际返回完整列表。

### 5.4 AdjacencyDTO（邻接表）

| 字段 | 类型 | 说明 |
|---|---|---|
| `cityId` | int | 城市 id |
| `cityName` | string | 城市名 |
| `neighbors` | array | 邻居列表 |

邻居：

| 字段 | 类型 | 说明 |
|---|---|---|
| `toId` | int | 邻居城市 id |
| `toName` | string | 邻居城市名 |
| `weight` | double | 边权，km |

```json
{
  "cityId": 1000,
  "cityName": "北京",
  "neighbors": [
    { "toId": 1001, "toName": "上海", "weight": 1068.39 }
  ]
}
```

### 5.5 GraphDTO（G6 渲染）

| 字段 | 类型 | 说明 |
|---|---|---|
| `nodes` | array | `{id, name}`，`id` 是字符串 |
| `edges` | array | `{source, target, weight}`，端点也是字符串 |
| `nodeCount` | int | 节点数 |
| `edgeCount` | int | 边数 |

```json
{
  "nodes": [ { "id": "1000", "name": "北京" } ],
  "edges": [ { "source": "1000", "target": "1001", "weight": 1068.39 } ],
  "nodeCount": 30,
  "edgeCount": 435
}
```

> G6 的 id 用字符串，所以这里转成 `"1000"`；业务接口仍然用 int。

### 5.6 MstDTO

| 字段 | 类型 | 说明 |
|---|---|---|
| `algorithm` | string | `prim` 或 `kruskal` |
| `nodeCount` | int | 节点数 |
| `edgeCount` | int | 选中边数；连通时为 `n-1` |
| `connected` | boolean | 是否连通 |
| `totalWeight` | double | 总权值，km，2 位 |
| `edges` | array | MST 的边 |
| `elapsedNanos` | long | 算法耗时 |
| `graphVersion` | int | 数据版本 |

`edges` 的每一项：

| 字段 | 类型 | 说明 |
|---|---|---|
| `fromId` | int | 端点一 |
| `fromName` | string | 端点一名称 |
| `toId` | int | 端点二 |
| `toName` | string | 端点二名称 |
| `weight` | double | 边权 |

```json
{
  "algorithm": "prim",
  "nodeCount": 8,
  "edgeCount": 7,
  "connected": true,
  "totalWeight": 4360.77,
  "edges": [
    { "fromId": 1001, "fromName": "上海", "toId": 1006, "toName": "杭州", "weight": 163.27 }
  ],
  "elapsedNanos": 12500,
  "graphVersion": 3
}
```

> 上面是 8 城演示数据，总权值 4360.77。正式 30 城为 29 条边，总权值由程序算，不要手写。
> 内部 `Edge` 用 `startId/endId`，对外统一用 `fromId/toId`，在 `MstService` 转 DTO 时映射。

### 5.7 PathDTO

| 字段 | 类型 | 说明 |
|---|---|---|
| `mode` | string | `graph` 或 `tree` |
| `found` | boolean | 是否找到路径 |
| `fromId` / `fromName` | int / string | 起点 |
| `toId` / `toName` | int / string | 终点 |
| `cost` | double / null | 总代价；不可达为 null |
| `hops` | int | 边数 |
| `path` | array | `{id, name}` 的完整路径 |
| `elapsedNanos` | long | 算法耗时 |
| `graphVersion` | int | 数据版本 |

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

> 完全图 + 欧氏距离，直连通常就是最短路；`hops=1` 是正常结果，不是 bug。
> `mode=tree` 在 MST 上跑，路径唯一，可能多跳，只用于答辩对照。

### 5.8 登录相关

`LoginRequest`：

| 字段 | 类型 | 约束 |
|---|---|---|
| `username` | string | 必填 |
| `password` | string | 必填 |

`LoginResponse`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `token` | string | UUID |
| `user` | UserDTO | 当前用户 |

`UserDTO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 用户 id |
| `username` | string | 用户名 |
| `role` | string | `ADMIN` 或 `USER` |

## 6. 接口清单

| 方法 | 路径 | 权限 | 请求 | 响应 |
|---|---|---|---|---|
| POST | `/api/auth/login` | 公开 | `LoginRequest` | `LoginResponse` |
| GET | `/api/auth/me` | 登录 | `Authorization` | `UserDTO` |
| POST | `/api/auth/logout` | 登录 | `Authorization` | `null` |
| GET | `/api/cities` | 登录 | — | `CityDTO[]` |
| GET | `/api/cities/sorted?reference={id}` | 登录 | `reference` 可选，默认 1000 | `SortedCitiesDTO` |
| GET | `/api/cities/{id}` | 登录 | — | `CityDTO` |
| POST | `/api/cities` | 管理员 | `CityRequest` | `CityDTO` |
| PUT | `/api/cities/{id}` | 管理员 | `CityRequest` | `CityDTO` |
| DELETE | `/api/cities/{id}` | 管理员 | — | `null` |
| GET | `/api/graph` | 登录 | — | `GraphDTO` |
| GET | `/api/graph/adjacency` | 登录 | — | `AdjacencyDTO[]` |
| GET | `/api/mst?algorithm=prim\|kruskal` | 登录 | `algorithm` 默认 `prim` | `MstDTO` |
| GET | `/api/paths/shortest?from={id}&to={id}&mode=graph\|tree` | 登录 | `mode` 默认 `graph` | `PathDTO` |
| POST | `/api/admin/reset` | 管理员 | — | `null` |

> 权限目前只是注解和约定；`AuthInterceptor` 还是放行骨架，黄予函补完后才真正 401/403。

## 7. 校验和边界

| 场景 | 行为 |
|---|---|
| `name` 为空或超过 20 字符 | 400，`errors` 里给字段错误 |
| `longitude` 不在 [-180,180] | 400 |
| `latitude` 不在 [-90,90] | 400 |
| `name` 重复 | 409 |
| `id` 不存在 | 404 |
| `from`/`to` 不存在 | 404 |
| `from == to` | 200，`path` 只有一个城市，`cost=0` |
| `mode` 不是 graph/tree | 400 |
| `algorithm` 不是 prim/kruskal | 400 |
| 图不连通 | MST `connected=false`；路径 `found=false` |
| 删到 0 个城市 | MST 空，路径 404，页面提示 |
| 普通用户调写接口 | 403 |
| 没带 token | 401 |

## 8. 错误示例

```json
{ "code": 404, "message": "城市不存在：9999", "data": null }
```

```json
{ "code": 409, "message": "城市名称已存在：北京", "data": null }
```

```json
{ "code": 403, "message": "没有权限执行此操作", "data": null }
```

## 9. 变更记录

| 日期 | 变更 | 影响 |
|---|---|---|
| 2026-10-28 | 初版，按 v1.1 第 7 章整理 | 前端可据此做 mock |
| 2026-11-04 | 补齐 DTO、认证、错误体、校验、graphVersion；后端骨架按本文件生成 | 前后端以此为唯一契约 |

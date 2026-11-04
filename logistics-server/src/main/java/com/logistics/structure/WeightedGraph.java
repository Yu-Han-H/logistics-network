package com.logistics.structure;

import com.logistics.model.City;
import com.logistics.model.Edge;
import com.logistics.util.DistanceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
    Map不是 "一个类"，它是一个接口（interface），定义了一套键值对存储的 "标准"，HashMap 是它的一个实现类。
    Map<Key值，Value值>
 */

/*
Collection（存单个元素）          Map（存键值对）
   ├── List：有顺序、可重复          ├── HashMap
   ├── Set：无顺序、不可重复         ├── TreeMap
   └── Queue：队列                  └── ...
 */
/**
 * 带权无向图，邻接表实现。
 *
 * <p>adjacency 里每条无向边在两端各存一次，共用同一个 Edge 对象，
 * 所以两个方向的权值天然相等。edgeCount 记的是「无向边条数」，
 * 邻接记录数是它的 2 倍：8 城 28 条边、56 条记录；30 城 435 / 870。
 *
 * <p>{@link Edge} 构造时会把端点规范成 {@code startId < endId}，
 * 因此无论调用方传 {@code addEdge(a, b)} 还是 {@code addEdge(b, a)}，
 * 图里都只有同一条边，不会出现方向不对称。
 *
 * <p>不变量：无自环、无重复边、无悬挂边、每条边两个方向都在、
 * 每条边 {@code startId < endId}、{@code sum(degree) == 2 * edgeCount}。
 */
public class WeightedGraph {

    private final Map<Integer, List<Edge>> adjacency = new HashMap<>();
    private int edgeCount;

    // ------------------------------------------------------------------
    // 顶点
    // ------------------------------------------------------------------

    //通过cityId来判断该城市是否在图中出现过
    public boolean addVertex(int cityId) {
        if (adjacency.containsKey(cityId)) {    //map.containsKey() --判断是否存在，有为true，无为false；
            return false;
        }
        adjacency.put(cityId, new ArrayList<>());
        return true;
    }

    public boolean containsVertex(int cityId) {
        return adjacency.containsKey(cityId);
    }

    //vertexCount作用是返回图的大小
    public int vertexCount() {
        return adjacency.size();
    }

    /** 按 id 升序，保证邻接表输出顺序稳定。 */
    public Set<Integer> vertices() {
        return Collections.unmodifiableSet(new TreeSet<>(adjacency.keySet()));
    }//把传进去的 Set 包一层“只读外壳”，返回后调用方只能读、不能改。

    /**
     * 顶点 id 的升序快照，返回基本类型数组。
     *
     * <p>Prim / Kruskal 需要把城市 id 映射成 0..n-1 的数组下标。
     * 直接遍历 {@code Set<Integer>} 会反复拆箱，这里一次性转成 {@code int[]}，
     * 算法内部就不再出现 Integer 拆箱。
     */
    //把Integer的key值转化为int型ids[]的值
    public int[] vertexIds() {
        TreeSet<Integer> sorted = new TreeSet<>(adjacency.keySet());
        int[] ids = new int[sorted.size()];
        int index = 0;
        for (Integer cityId : sorted) {
            ids[index++] = cityId.intValue();
        }
        return ids;
    }

    // ------------------------------------------------------------------
    // 边
    // ------------------------------------------------------------------

    /**
     * 加边，两个方向各存一次。含查重，代价 O(deg)；
     * n=30 下这点开销可以忽略，换来的是不会悄悄出现重复边。
     *
     * <p>Edge 构造时会把 fromId/toId 规范成 {@code startId < endId}，
     * 所以传参顺序不影响结果，两个方向共用同一个 Edge 对象。
     */
    public void addEdge(int fromId, int toId, double weight) {
        requireVertex(fromId);
        requireVertex(toId);
        Edge edge = new Edge(fromId, toId, weight);
        if (hasEdge(edge.getStartId(), edge.getEndId())) {
            throw new IllegalArgumentException("边已存在：" + edge);
        }
        adjacency.get(edge.getStartId()).add(edge);
        adjacency.get(edge.getEndId()).add(edge);
        edgeCount++;
    }

    public boolean hasEdge(int a, int b) {
        for (Edge edge : neighbors(a)) {
            if (edge.connects(a, b)) {
                return true;
            }
        }
        return false;
    }

    public int edgeCount() {
        return edgeCount;
    }

    /**
     * 邻接记录条数，等于所有顶点度数之和，正常应为 {@code 2 * edgeCount}。
     * 单独提供这个方法，避免调用方为了统计记录数去拆箱 Stream&lt;Integer&gt;，
     * 也能在自检时直接验证「每条边存两次」。
     */

    /*
        adjacencyRecordCount作用：统计邻接表里一共有多少条记录
        1000 → [7 条]   ┐
        1001 → [7 条]   │
        1002→ [7 条]   │
        ...            ├── 8 行 × 每行 7 条 = 56
        1007 → [7 条]   ┘
        30 城时就是 30 × 29 = 870。
     */
    public int adjacencyRecordCount() {
        int count = 0;
        for (List<Edge> edges : adjacency.values()) {
            count += edges.size();
        }
        return count;
    }

    //degree作用：计算某个城市的度
    public int degree(int cityId) {
        List<Edge> edges = adjacency.get(cityId);
        return edges == null ? 0 : edges.size();
    }

    /** 只读邻居边；城市不存在返回空列表，不抛异常。 */
    public List<Edge> neighbors(int cityId) {
        List<Edge> edges = adjacency.get(cityId);
        return edges == null ? List.of() : Collections.unmodifiableList(edges);
    }

    /** 给 Prim / Dijkstra 用的邻居 id 列表。 */
    public List<Integer> neighborIds(int cityId) {
        List<Integer> ids = new ArrayList<>(degree(cityId));
        for (Edge edge : neighbors(cityId)) {
            ids.add(edge.other(cityId));
        }
        return ids;
    }

    /** 所有无向边，每条只出现一次（fromId < toId）。Kruskal 排序用这个。 */
    public List<Edge> edges() {
        List<Edge> result = new ArrayList<>(edgeCount);
        for (Map.Entry<Integer, List<Edge>> entry : adjacency.entrySet()) {
            for (Edge edge : entry.getValue()) {
                if (edge.getStartId() == entry.getKey()) {
                    result.add(edge);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 删改
    // ------------------------------------------------------------------

    public boolean removeEdge(int a, int b) {
        // 无自环；a == b 时 from 和 to 是同一个列表，removeIf 会执行两次，
        // 第二次找不到边，容易把 edgeCount 弄乱，所以直接返回 false。
        if (a == b) {
            return false;
        }
        List<Edge> from = adjacency.get(a);
        List<Edge> to = adjacency.get(b);
        if (from == null || to == null) {
            return false;
        }

        boolean removedFrom = from.removeIf(edge -> edge.connects(a, b));
        boolean removedTo = to.removeIf(edge -> edge.connects(a, b));

        // 正常情况下两个方向要么都删掉、要么都不存在。
        // 只删掉一边说明图已经被破坏，不能悄悄把 edgeCount 减一了事。
        if (removedFrom != removedTo) {
            throw new IllegalStateException(
                "边 " + a + " - " + b + " 只有单向记录，图结构已损坏");
        }
        if (removedFrom) {
            edgeCount--;
            return true;
        }
        return false;
    }

    /** 删点 O(deg)：先把它关联的边从对面删干净，再摘节点，避免悬挂边。 */
    public boolean removeVertex(int cityId) {
        List<Edge> incident = adjacency.remove(cityId);
        if (incident == null) {
            return false;
        }
        for (Edge edge : incident) {
            int other = edge.other(cityId);
            List<Edge> otherEdges = adjacency.get(other);
            if (otherEdges != null) {
                otherEdges.removeIf(e -> e.connects(cityId, other));
            }
            edgeCount--;
        }
        return true;
    }

    /**
     * 坐标变了以后更新边权。Edge 不可变，所以删掉重建。
     *
     * <p>先校验新权值，再删旧边：如果先删后校验，负权值会让
     * {@link Edge} 构造失败，旧边已经没了，图就少一条边。
     */
    public void updateWeight(int a, int b, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("边权不能为负：" + weight);
        }
        if (!hasEdge(a, b)) {
            throw new IllegalArgumentException("边不存在：" + a + " - " + b);
        }
        removeEdge(a, b);
        addEdge(a, b, weight);
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    /**
     * 按城市集合建完全图：任意两城之间都有边，边权用 DistanceUtil 计算。
     *
     * <p>注意内层循环从 i + 1 开始。写成两层全排列会把每条边加两次，
     * addEdge 会直接抛「边已存在」。
     */
    public static WeightedGraph completeGraph(List<City> cities) {
        if (cities == null) {
            throw new IllegalArgumentException("城市列表不能为 null");
        }
        WeightedGraph graph = new WeightedGraph();
        Set<Integer> cityIds = new HashSet<>();
        for (City city : cities) {
            if (city == null) {
                throw new IllegalArgumentException("城市列表不能包含 null");
            }
            // 重复 id 会让后面 addEdge 变成自环，或者同一对点建两次边；
            // 这里提前报错，报错信息比「边已存在」清楚。
            if (!cityIds.add(city.getId())) {
                throw new IllegalArgumentException("城市 id 重复：" + city.getId());
            }
            graph.addVertex(city.getId());
        }
        for (int i = 0; i < cities.size(); i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                City a = cities.get(i);
                City b = cities.get(j);
                graph.addEdge(a.getId(), b.getId(), DistanceUtil.distance(a, b));
            }
        }
        return graph;
    }

    // ------------------------------------------------------------------
    // 校验与调试
    // ------------------------------------------------------------------

    /** 校验邻接表不变量，测试里每次操作后调一次。 */
    public void checkInvariants() {
        int counted = 0;
        for (Map.Entry<Integer, List<Edge>> entry : adjacency.entrySet()) {
            int cityId = entry.getKey();
            Set<Integer> seen = new HashSet<>();
            for (Edge edge : entry.getValue()) {
                int other = edge.other(cityId);
                if (!adjacency.containsKey(other)) {
                    throw new IllegalStateException("悬挂边：" + cityId + " - " + other);
                }
                if (!seen.add(other)) {
                    throw new IllegalStateException("重复边：" + cityId + " - " + other);
                }
                if (edge.getStartId() >= edge.getEndId()) {
                    throw new IllegalStateException("边未规范化：" + edge);
                }
                // 必须是对面列表里的同一个 Edge 对象；只 equals 相等不够，
                // 那只能说明端点相同，不能保证两个方向权值一致。
                boolean mirrored = false;
                for (Edge candidate : adjacency.get(other)) {
                    if (candidate == edge) {
                        mirrored = true;
                        break;
                    }
                }
                if (!mirrored) {
                    throw new IllegalStateException("单向边：" + cityId + " - " + other);
                }
                // 每条边只在较小端点那一侧计一次
                if (edge.getStartId() == cityId) {
                    counted++;
                }
            }
        }
        if (counted != edgeCount) {
            throw new IllegalStateException("edgeCount 不一致：字段=" + edgeCount + "，实际=" + counted);
        }
        int records = adjacencyRecordCount();
        if (records != edgeCount * 2) {
            throw new IllegalStateException(
                "邻接记录数不一致：记录=" + records + "，edgeCount=" + edgeCount);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("WeightedGraph{vertices=")
            .append(vertexCount()).append(", edges=").append(edgeCount).append("}\n");
        for (Integer cityId : vertices()) {
            builder.append(cityId).append(": ").append(neighbors(cityId)).append('\n');
        }
        return builder.toString();
    }

    //requireVertex作用是通过cityId判断该城市是否在图中
    private void requireVertex(int cityId) {
        if (!adjacency.containsKey(cityId)) {
            throw new IllegalArgumentException("城市不存在：" + cityId);
        }
    }
}

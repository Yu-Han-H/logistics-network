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

/**
 * 带权无向图，邻接表实现。
 *
 * <p>adjacency 里每条无向边在两端各存一次，共用同一个 Edge 对象，
 * 所以两个方向的权值天然相等。edgeCount 记的是「无向边条数」，
 * 邻接记录数是它的 2 倍：8 城 28 条边、56 条记录；30 城 435 / 870。
 *
 * <p>不变量：无自环、无重复边、无悬挂边、每条边两个方向都在、
 * edgeCount 等于实际边条数。
 */
public class WeightedGraph {

    private final Map<Integer, List<Edge>> adjacency = new HashMap<>();
    private int edgeCount;

    // ------------------------------------------------------------------
    // 顶点
    // ------------------------------------------------------------------

    public boolean addVertex(int cityId) {
        if (adjacency.containsKey(cityId)) {
            return false;
        }
        adjacency.put(cityId, new ArrayList<>());
        return true;
    }

    public boolean containsVertex(int cityId) {
        return adjacency.containsKey(cityId);
    }

    public int vertexCount() {
        return adjacency.size();
    }

    /** 按 id 升序，保证邻接表输出顺序稳定。 */
    public Set<Integer> vertices() {
        return Collections.unmodifiableSet(new TreeSet<>(adjacency.keySet()));
    }

    // ------------------------------------------------------------------
    // 边
    // ------------------------------------------------------------------

    /**
     * 加边，两个方向各存一次。含查重，代价 O(deg)；
     * n=30 下这点开销可以忽略，换来的是不会悄悄出现重复边。
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
        List<Edge> from = adjacency.get(a);
        List<Edge> to = adjacency.get(b);
        if (from == null || to == null) {
            return false;
        }
        boolean removedFrom = from.removeIf(edge -> edge.connects(a, b));
        boolean removedTo = to.removeIf(edge -> edge.connects(a, b));
        if (removedFrom && removedTo) {
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

    /** 坐标变了以后更新边权。Edge 不可变，所以删掉重建。 */
    public void updateWeight(int a, int b, double weight) {
        if (!removeEdge(a, b)) {
            throw new IllegalArgumentException("边不存在：" + a + " - " + b);
        }
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
        WeightedGraph graph = new WeightedGraph();
        for (City city : cities) {
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

    /** 校验四条不变量，测试里每次操作后调一次。 */
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
                if (!adjacency.get(other).contains(edge)) {
                    throw new IllegalStateException("单向边：" + cityId + " - " + other);
                }
                if (edge.getEndId() == cityId) {
                    counted++;
                }
            }
        }
        if (counted != edgeCount) {
            throw new IllegalStateException("edgeCount 不一致：字段=" + edgeCount + "，实际=" + counted);
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

    private void requireVertex(int cityId) {
        if (!adjacency.containsKey(cityId)) {
            throw new IllegalArgumentException("城市不存在：" + cityId);
        }
    }
}

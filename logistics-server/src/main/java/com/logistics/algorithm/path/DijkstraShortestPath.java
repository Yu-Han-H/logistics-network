package com.logistics.algorithm.path;

import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;
import com.logistics.util.DistanceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单源最短路（Dijkstra），数组版 O(V²)，在**原图**上跑（文档 4.6）。
 *
 * <p>做法：
 * <ol>
 *   <li>{@code dist[]} 全部设为无穷，{@code dist[from] = 0}；</li>
 *   <li>每轮找未访问点中 dist 最小的 u，标记已访问；</li>
 *   <li>用 u 的邻居松弛：{@code dist[u] + w(u,v) < dist[v]} 时更新，并记 {@code prev[v] = u}；</li>
 *   <li>从 to 沿 prev 往回走，得到路径后反转。</li>
 * </ol>
 *
 * <p>为什么用数组版：n = 30 时 O(V²) 只有 900 次操作，比堆更简单，
 * 也不用处理 decrease-key（文档 1.5、4.5）。
 *
 * <p>为什么结果稳定：顶点按 id 升序取下标，每轮扫描时距离相同取下标小的点，
 * 松弛用严格小于，所以等权最短路每次都会选出同一条（文档 4.6 边界）。
 */
public class DijkstraShortestPath {

    /** 不可达的标记，用正无穷而不是 -1，方便直接参与加法比较。 */
    public static final double INF = Double.POSITIVE_INFINITY;

    private final WeightedGraph graph;

    public DijkstraShortestPath(WeightedGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("图不能为 null");
        }
        this.graph = graph;
    }

    /**
     * 求 from 到 to 的最短路径。
     *
     * @param fromId 起点城市 id
     * @param toId   终点城市 id
     * @return 路径结果；不可达时 {@code found = false}、路径为空
     * @throws IllegalArgumentException 起点或终点不在图中
     */
    public PathResult shortestPath(int fromId, int toId) {
        long startNanos = System.nanoTime();

        int[] ids = graph.vertexIds();
        int n = ids.length;

        Map<Integer, Integer> indexById = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            indexById.put(ids[i], i);
        }

        Integer fromIndexBoxed = indexById.get(fromId);
        Integer toIndexBoxed = indexById.get(toId);
        if (fromIndexBoxed == null) {
            throw new IllegalArgumentException("起点城市不存在：" + fromId);
        }
        if (toIndexBoxed == null) {
            throw new IllegalArgumentException("终点城市不存在：" + toId);
        }
        int fromIndex = fromIndexBoxed;
        int toIndex = toIndexBoxed;

        // from == to：路径只有自己，代价 0（文档 4.6 边界）
        if (fromIndex == toIndex) {
            return new PathResult(true, fromId, toId, 0.0, List.of(fromId),
                System.nanoTime() - startNanos);
        }

        double[] dist = new double[n];
        Arrays.fill(dist, INF);
        int[] prev = new int[n];
        Arrays.fill(prev, -1);
        boolean[] visited = new boolean[n];
        dist[fromIndex] = 0.0;

        while (true) {
            // 找未访问点中 dist 最小的
            int u = -1;
            double best = INF;
            for (int i = 0; i < n; i++) {
                if (!visited[i] && dist[i] < best) {
                    best = dist[i];
                    u = i;
                }
            }
            if (u == -1) {
                break;              // 剩下的点都不可达
            }
            visited[u] = true;
            if (u == toIndex) {
                break;              // 终点已确定，不必再松弛
            }

            for (Edge edge : graph.neighbors(ids[u])) {
                int v = indexById.get(edge.other(ids[u]));
                if (visited[v]) {
                    continue;
                }
                double candidate = dist[u] + edge.getWeight();
                if (candidate < dist[v]) {
                    dist[v] = candidate;
                    prev[v] = u;
                }
            }
        }

        if (dist[toIndex] == INF) {
            return new PathResult(false, fromId, toId, 0.0, List.of(),
                System.nanoTime() - startNanos);
        }

        List<Integer> path = new ArrayList<>(n);
        for (int cursor = toIndex; cursor != -1; cursor = prev[cursor]) {
            path.add(ids[cursor]);
        }
        Collections.reverse(path);

        // 边权都已取整到 2 位，累加后可能有尾差，统一 round2
        double cost = DistanceUtil.round2(dist[toIndex]);
        return new PathResult(true, fromId, toId, cost, path,
            System.nanoTime() - startNanos);
    }
}

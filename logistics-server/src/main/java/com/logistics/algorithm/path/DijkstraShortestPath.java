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
import java.util.PriorityQueue;

/**
 * Dijkstra 单源最短路径算法，求带权无向图上任意两个城市之间的最短路径
 */
public class DijkstraShortestPath {

    // 无穷大，表示当前城市还不可达。
    private static final double INF = Double.POSITIVE_INFINITY;

    // 优先队列节点：距离小的先出队；距离相同按城市 id 升序，保证等权最短路每次运行结果一致
    private static class Node implements Comparable<Node> {
        int cityId;
        double distance;

        Node(int cityId, double distance) {
            this.cityId = cityId;
            this.distance = distance;
        }

        @Override 
        public int compareTo(Node other) {
            int byDistance=Double.compare(this.distance, other.distance);
            if(byDistance!=0)
                return byDistance;
            return Integer.compare(this.cityId, other.cityId);
        }
    }

    /**
     * 求 fromId 到 toId 的最短路径。
     *
     * 最短路径在原带权无向图上运行，不是在 MST 上运行；MST 只表示铺线总成本最低
     * 边权必须非负，DistanceUtil 计算出的欧氏距离天然满足
     *
     * @param graph 邻接表图
     * @param fromId 起点城市 id
     * @param toId 终点城市 id
     * @return 路径结果；不可达时 found=false、cost=null、path 为空
     * @throws IllegalArgumentException 图为 null，或 fromId/toId 不在图中时抛出
     */
    public PathResult shortestPath(WeightedGraph graph, int fromId, int toId) {
        if (graph == null)
            throw new IllegalArgumentException("图不能为 null");
        if (!graph.containsVertex(fromId))
            throw new IllegalArgumentException("起点城市不存在：" + fromId);
        if (!graph.containsVertex(toId))
            throw new IllegalArgumentException("终点城市不存在：" + toId);

        long startNanos = System.nanoTime();

        // from == to：路径只有一个城市，代价为 0
        if (fromId == toId) {
            return new PathResult(true, 0.0, 0, List.of(fromId),
                System.nanoTime() - startNanos);
        }

        // 城市 id 可能不连续（1000 起），先映射成 0..n-1 的数组下标
        int[] cityIds = graph.vertexIds();
        Map<Integer, Integer> indexByCityId = new HashMap<>(cityIds.length * 2);
        
        for (int i = 0; i < cityIds.length; i++)
            indexByCityId.put(cityIds[i], i);

        int fromIndex = indexOf(indexByCityId, fromId);
        int toIndex = indexOf(indexByCityId, toId);

        double[] distances = new double[cityIds.length];
        int[] previous = new int[cityIds.length];
        boolean[] processed = new boolean[cityIds.length];

        Arrays.fill(distances, INF);
        Arrays.fill(previous, -1);

        PriorityQueue<Node> unprocessed = new PriorityQueue<>();
        distances[fromIndex] = 0.0;
        unprocessed.add(new Node(fromId, 0.0));

        while (!unprocessed.isEmpty()) {
            Node current = unprocessed.poll();
            int u = indexOf(indexByCityId, current.cityId);

            // 懒删除：同一个城市可能多次入队，旧距离已经过期
            if (processed[u] || current.distance > distances[u])
                continue;

            processed[u] = true;

            // 已经确定终点的最短路，可以提前结束
            if (u == toIndex)
                break;

            for (Edge edge : graph.neighbors(cityIds[u])) {
                int v = indexOf(indexByCityId, edge.other(cityIds[u]));
                if (processed[v])
                    continue;

                double candidate = distances[u] + edge.getWeight();
                // 严格小于才更新：等权时保留先到的路径，配合优先队列的城市 id 顺序保证结果稳定
                if (candidate < distances[v]) {
                    distances[v] = candidate;
                    previous[v] = u;
                    unprocessed.add(new Node(cityIds[v], candidate));
                }
            }
        }

        // 终点没有出队，说明从起点不可达
        if (!processed[toIndex]) {
            return new PathResult(false, null, 0, List.of(),
                System.nanoTime() - startNanos);
        }

        List<Integer> path = buildPath(cityIds, previous, fromIndex, toIndex);
        return new PathResult(true, DistanceUtil.round2(distances[toIndex]),
            path.size() - 1, path, System.nanoTime() - startNanos);
    }

    /**
     * 从 previous 数组回溯路径，再反转成 from -> to 的顺序。
     *
     * @param cityIds 下标到城市 id 的映射
     * @param previous 每个城市的前驱下标
     * @param fromIndex 起点下标
     * @param toIndex 终点下标
     * @return 完整路径，首元素是起点，末元素是终点
     */
    private List<Integer> buildPath(int[] cityIds, int[] previous, int fromIndex, int toIndex) {
        List<Integer> reversed = new ArrayList<>();
        int cursor = toIndex;
        while (cursor != -1) {
            reversed.add(cityIds[cursor]);
            if (cursor == fromIndex) {
                break;
            }
            cursor = previous[cursor];
        }
        Collections.reverse(reversed);
        return List.copyOf(reversed);
    }

    /**
     * 把城市 id 换成数组下标。
     *
     * @param indexByCityId 城市 id 到下标的映射
     * @param cityId 城市 id
     * @return 数组下标
     * @throws IllegalStateException 城市不在图中时抛出
     */
    private int indexOf(Map<Integer, Integer> indexByCityId, int cityId) {
        Integer index = indexByCityId.get(cityId);
        if (index == null) {
            throw new IllegalStateException("图中不存在城市：" + cityId);
        }
        return index.intValue();
    }

    /**
     * 最短路径查询结果。
     */
    public static class PathResult {
        private final boolean found;
        private final Double cost;
        private final int hops;
        private final List<Integer> path;
        private final long elapsedNanos;

        /**
         * 构造路径结果。
         *
         * @param found 是否找到路径
         * @param cost 总代价；不可达时为 null
         * @param hops 边数
         * @param path 完整路径，元素是城市 id
         * @param elapsedNanos 算法耗时，单位纳秒
         */
        public PathResult(boolean found, Double cost, int hops,
                          List<Integer> path, long elapsedNanos) {
            if (path == null) {
                throw new IllegalArgumentException("路径不能为 null");
            }
            this.found = found;
            this.cost = cost;
            this.hops = hops;
            this.path = List.copyOf(path);
            this.elapsedNanos = elapsedNanos;
        }

        public boolean isFound() {
            return found;
        }

        public Double getCost() {
            return cost;
        }

        public int getHops() {
            return hops;
        }

        public List<Integer> getPath() {
            return path;
        }

        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }
}
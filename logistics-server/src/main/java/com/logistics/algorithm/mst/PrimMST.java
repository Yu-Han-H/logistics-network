package com.logistics.algorithm.mst;

import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prim 最小生成树算法。
 *
 * 
 *   把所有顶点分成「已选」和「未选」两组；
 *   {@code minWeight[v]} 记录未选点 v 到已选集合的最短边权；
 *   每轮从未选点里挑 {@code minWeight} 最小的点加入集合，并更新它的邻居；
 *   重复 n-1 次，得到 n-1 条边。
 * 
 *
 * <p>没有用 {@code PriorityQueue}：Java 的优先队列不支持 decrease-key，
 * 懒删除写法反而绕；n=30 时数组扫描 O(n²)=900 次，更简单也更快。
 *
 * 等权处理：
 * 
 *   选点时权值相同，选城市 id 较小的；
 *   更新父节点时权值相同，选城市 id 较小的父节点。
 *
 * 这样同一张图重复运行的结果固定，Prim 和 Kruskal 也更容易对上。
 *
 * 不连通时继续从下一个未访问点起算，返回最小生成森林，
 * {@link MstResult#isConnected()} 为 false。
 *
 * <p>时间复杂度 O(V²)，空间复杂度 O(V)。
 */
public class PrimMST {

    /** 无穷大，表示当前点还不可达。 */
    private static final double INF = Double.POSITIVE_INFINITY;

    /**
     * 对给定无向图求最小生成树。
     *
     * @param graph 邻接表图，不能为 null
     * @return MST 结果；n=0 时边为空、connected=false，n=1 时边为空、connected=true
     * @throws IllegalArgumentException graph 为 null 时抛出
     */
    public MstResult primMST(WeightedGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("图不能为 null");
        }
        long startNanos = System.nanoTime();

        int nodeCount = graph.vertexCount();
        if (nodeCount == 0) {
            // 文档 4.5：没有城市时总权值 0、边为空、connected=false
            return new MstResult(MstResult.PRIM, 0, List.of(), false,
                System.nanoTime() - startNanos);
        }

        // 城市 id 可能不连续（1000 起），先映射成 0..n-1 的数组下标
        int[] cityIds = graph.vertexIds();
        Map<Integer, Integer> indexByCityId = new HashMap<>(nodeCount * 2);
        for (int i = 0; i < nodeCount; i++) {
            indexByCityId.put(cityIds[i], i);
        }

        double[] minWeight = new double[nodeCount];
        int[] parent = new int[nodeCount];
        boolean[] inTree = new boolean[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            minWeight[i] = INF;
            parent[i] = -1;
        }

        List<Edge> mstEdges = new ArrayList<>(Math.max(0, nodeCount - 1));
        int componentCount = 0;

        // 外层循环处理不连通图：每个连通分量跑一次 Prim
        for (int root = 0; root < nodeCount; root++) {
            if (inTree[root]) {
                continue;
            }
            componentCount++;
            minWeight[root] = 0.0;

            while (true) {
                int u = findMinVertex(minWeight, inTree, cityIds);
                if (u == -1) {
                    // 当前分量已经扩展完，剩下的点属于其他连通分量
                    break;
                }
                inTree[u] = true;

                // 根节点没有父边；其余点加入时，父边就是 MST 的一条边
                if (parent[u] != -1) {
                    mstEdges.add(new Edge(cityIds[parent[u]], cityIds[u], minWeight[u]));
                }

                // 用新加入的点更新邻居到已选集合的最小边权
                for (Edge edge : graph.neighbors(cityIds[u])) {
                    int v = indexOf(indexByCityId, edge.other(cityIds[u]));
                    if (inTree[v]) {
                        continue;
                    }
                    double weight = edge.getWeight();
                    boolean lighter = weight < minWeight[v];
                    boolean sameWeightButSmallerParent = weight == minWeight[v]
                        && parent[v] != -1
                        && cityIds[u] < cityIds[parent[v]];
                    if (lighter || sameWeightButSmallerParent) {
                        minWeight[v] = weight;
                        parent[v] = u;
                    }
                }
            }
        }

        boolean connected = componentCount == 1;
        return new MstResult(MstResult.PRIM, nodeCount, mstEdges, connected,
            System.nanoTime() - startNanos);
    }

    /**
     * 在未选点中找 {@code minWeight} 最小的点；权值相同取城市 id 较小的。
     *
     * @return 选中的数组下标；所有未选点都不可达时返回 -1
     */
    private int findMinVertex(double[] minWeight, boolean[] inTree, int[] cityIds) {
        int best = -1;
        double bestWeight = INF;

        for (int v = 0; v < minWeight.length; v++) {
            if (inTree[v]) {
                continue;
            }
            boolean lighter = minWeight[v] < bestWeight;
            // best == -1 时不比较 id，避免把无穷大的点选进来
            boolean sameWeightButSmallerId = minWeight[v] == bestWeight
                && best != -1
                && cityIds[v] < cityIds[best];
            if (lighter || sameWeightButSmallerId) {
                best = v;
                bestWeight = minWeight[v];
            }
        }
        return best;
    }

    /**
     * 把城市 id 换成数组下标。
     *
     * 正常建图不会走到 null 分支；这里显式判断，防御邻接表损坏，
     */
    private int indexOf(Map<Integer, Integer> indexByCityId, int cityId) {
        Integer index = indexByCityId.get(cityId);
        if (index == null) {
            throw new IllegalStateException("图中不存在城市：" + cityId);
        }
        return index.intValue();
    }
}

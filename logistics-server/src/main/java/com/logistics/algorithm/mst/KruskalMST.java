package com.logistics.algorithm.mst;

import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kruskal 最小生成树算法（边排序 + 并查集）。
 *
 * 
 *   取出图中所有无向边，按 (weight, fromId, toId) 升序排序；
 *   用并查集维护连通分量；
 *   从小到大取边，两端不在同一分量就选中并合并；
 *   选够 n-1 条边就停。
 * 
 *
 * 边的排序键和 {@link MstResult} 的输出顺序一致，保证每次运行结果稳定。
 * 并查集使用路径压缩 + 按秩合并，见 {@link UnionFind}。
 *
 * 不连通时返回最小生成森林，{@link MstResult#isConnected()} 为 false。
 *
 * 时间复杂度 O(Elog E)，空间复杂度 O(V + E)。
 */
public class KruskalMST {

    /**
     * 边的排序规则：权值升序；权值相同按 fromId 升序，再按 toId 升序。
     * 城市 id 是全局唯一整数，所以这个顺序是全序，结果可复现。
     */
    private static final Comparator<Edge> EDGE_ORDER = Comparator
        .comparingDouble(Edge::getWeight)
        .thenComparingInt(Edge::getStartId)
        .thenComparingInt(Edge::getEndId);

    /**
     * 对给定无向图求最小生成树。
     *
     * @param graph 邻接表图，不能为 null
     * @return MST 结果；n=0 时边为空、connected=false，n=1 时边为空、connected=true
     * @throws IllegalArgumentException graph 为 null 时抛出
     */
    public MstResult kruskalMST(WeightedGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("图不能为 null");
        }
        long startNanos = System.nanoTime();

        int nodeCount = graph.vertexCount();
        if (nodeCount == 0) {
            // 没有城市时总权值 0、边为空、connected=false
            return new MstResult(MstResult.KRUSKAL, 0, List.of(), false,
                System.nanoTime() - startNanos);
        }

        // 城市 id 可能不连续（1000 起），先映射成 0..n-1 的数组下标
        int[] cityIds = graph.vertexIds();
        Map<Integer, Integer> indexByCityId = new HashMap<>(nodeCount * 2);
        for (int i = 0; i < nodeCount; i++) {
            indexByCityId.put(cityIds[i], i);
        }

        List<Edge> allEdges = new ArrayList<>(graph.edges());
        allEdges.sort(EDGE_ORDER);

        UnionFind unionFind = new UnionFind(nodeCount);
        List<Edge> mstEdges = new ArrayList<>(Math.max(0, nodeCount - 1));

        for (Edge edge : allEdges) {
            int fromIndex = indexOf(indexByCityId, edge.getStartId());
            int toIndex = indexOf(indexByCityId, edge.getEndId());
            // union 返回 false 说明两端已经连通，加这条边会成环，跳过
            if (unionFind.union(fromIndex, toIndex)) {
                mstEdges.add(edge);
                if (mstEdges.size() == nodeCount - 1) {
                    break;
                }
            }
        }

        // 并查集只剩一个分量才算连通；n=1 时初始就是 1 个分量
        boolean connected = unionFind.componentCount() == 1;
        return new MstResult(MstResult.KRUSKAL, nodeCount, mstEdges, connected,
            System.nanoTime() - startNanos);
    }

    /**
     * 把城市 id 换成数组下标。
     *
     * <p>正常建图不会走到 null 分支；显式判断可避免 Integer 直接拆箱，
     * 也方便定位「边引用了一个不存在的城市」这种图损坏。
     */
    private int indexOf(Map<Integer, Integer> indexByCityId, int cityId) {
        Integer index = indexByCityId.get(cityId);
        if (index == null) {
            throw new IllegalStateException("图中不存在城市：" + cityId);
        }
        return index.intValue();
    }
}

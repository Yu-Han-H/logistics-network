package com.logistics.algorithm.mst;

import com.logistics.model.Edge;
import com.logistics.util.DistanceUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 最小生成树（或最小生成森林）的计算结果。
 *
 * <p>Prim 和 Kruskal 都返回这个对象，保证两种算法对外输出的字段完全一致，
 * Service 层只负责把 {@link Edge} 补上城市名并转成 DTO。
 *
 * 
 * 
 *   {@code algorithm}：{@code "prim"} 或 {@code "kruskal"}；
 *   {@code nodeCount}：图的顶点数；
 *   {@code edges}：选中的边，按 (weight, fromId, toId) 升序，输出稳定；
 *   {@code totalWeight}：所有选中边「已取整权值」之和，再整体保留 2 位；
 *   {@code connected}：图是否连通。连通时边数为 n-1；不连通时是最小生成森林；
 *   {@code elapsedNanos}：算法本身的耗时，单位纳秒。
 * 
 */
public class MstResult {

    /** Prim 算法的标识，和接口参数 algorithm=prim 对应。 */
    public static final String PRIM = "prim";

    /** Kruskal 算法的标识，和接口参数 algorithm=kruskal 对应。 */
    public static final String KRUSKAL = "kruskal";

    /**
     * 边的统一输出顺序：先按权值升序，权值相同按 fromId 升序，
     * 再按 toId 升序。对应文档 4.2 第 6 条 tie-break 规则。
     */
    private static final Comparator<Edge> EDGE_ORDER = Comparator
        .comparingDouble((Edge edge) -> edge.getWeight())
        .thenComparingInt(edge -> edge.getStartId())
        .thenComparingInt(edge -> edge.getEndId());

    private final String algorithm;
    private final int nodeCount;
    private final List<Edge> edges;
    private final double totalWeight;
    private final boolean connected;
    private final long elapsedNanos;

    /**
     * 构造 MST 结果。构造时统一排序边并计算总权值，
     * 避免两个算法各写一套汇总逻辑导致结果对不上。
     *
     * @param algorithm    算法标识，{@link #PRIM} 或 {@link #KRUSKAL}
     * @param nodeCount    图的顶点数
     * @param edges        选中的边，允许为空但不允许为 null
     * @param connected    图是否连通
     * @param elapsedNanos 算法耗时，单位纳秒
     */
    public MstResult(String algorithm, int nodeCount, List<Edge> edges,
                     boolean connected, long elapsedNanos) {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("算法标识不能为空");
        }
        if (nodeCount < 0) {
            throw new IllegalArgumentException("顶点数不能为负：" + nodeCount);
        }
        if (edges == null) {
            throw new IllegalArgumentException("边列表不能为 null");
        }

        List<Edge> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort(EDGE_ORDER);

        double[] weights = new double[sortedEdges.size()];
        for (int i = 0; i < sortedEdges.size(); i++) {
            weights[i] = sortedEdges.get(i).getWeight();
        }

        this.algorithm = algorithm;
        this.nodeCount = nodeCount;
        this.edges = List.copyOf(sortedEdges);
        // 文档 4.2 第 5 条：总权值 = 已取整边权之和，最后整体取整一次
        this.totalWeight = DistanceUtil.sumAndRound2(weights);
        this.connected = connected;
        this.elapsedNanos = elapsedNanos;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    /** 选中的边，只读；已按 (weight, fromId, toId) 升序排好。 */
    public List<Edge> getEdges() {
        return edges;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public boolean isConnected() {
        return connected;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    @Override
    public String toString() {
        return "MstResult{algorithm='" + algorithm + '\''
            + ", nodeCount=" + nodeCount
            + ", edgeCount=" + edges.size()
            + ", totalWeight=" + totalWeight
            + ", connected=" + connected
            + ", elapsedNanos=" + elapsedNanos
            + '}';
    }
}

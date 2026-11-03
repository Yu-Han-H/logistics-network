package com.logistics.algorithm.mst;

import com.logistics.model.City;
import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Prim / Kruskal / UnionFind 的单元测试。
 *
 * <p>覆盖文档 9.2 中 MST 相关用例：
 * 8 城已知总权值、30 城规模、n=0、n=1、不连通、等权 tie、并查集。
 */
class MstTest {

    /** 浮点比较容差，文档 9.2 要求用 0.01 以内。 */
    private static final double TOLERANCE = 0.005;

    @Test
    void eightCitiesMatchDocumentExample() {
        WeightedGraph graph = WeightedGraph.completeGraph(eightCities());

        MstResult prim = new PrimMST().primMST(graph);
        MstResult kruskal = new KruskalMST().kruskalMST(graph);

        assertThat(graph.vertexCount()).isEqualTo(8);
        assertThat(graph.edgeCount()).isEqualTo(28);

        assertThat(prim.isConnected()).isTrue();
        assertThat(prim.getNodeCount()).isEqualTo(8);
        assertThat(prim.getEdgeCount()).isEqualTo(7);
        assertThat(prim.getTotalWeight()).isCloseTo(4360.77, within(TOLERANCE));

        assertThat(kruskal.isConnected()).isTrue();
        assertThat(kruskal.getNodeCount()).isEqualTo(8);
        assertThat(kruskal.getEdgeCount()).isEqualTo(7);
        assertThat(kruskal.getTotalWeight()).isCloseTo(4360.77, within(TOLERANCE));

        // 文档 4.5：两种算法总权值必须相等；有等权边时不断言边集完全相同
        assertThat(prim.getTotalWeight())
            .isCloseTo(kruskal.getTotalWeight(), within(TOLERANCE));

        // 8 城权值互不相同，MST 唯一，可以按文档附录 B 精确断言边集
        assertThat(edgeKeys(prim)).containsExactly(
            "1001-1006", // 上海—杭州 163.27
            "1005-1006", // 武汉—杭州 565.37
            "1003-1004", // 成都—西安 608.82
            "1000-1007", // 北京—沈阳 627.82
            "1004-1005", // 西安—武汉 654.29
            "1002-1005", // 广州—武汉 829.24
            "1000-1004"  // 北京—西安 911.96
        );
        assertThat(edgeKeys(kruskal)).containsExactlyElementsOf(edgeKeys(prim));
    }

    @Test
    void thirtyCitiesFromSeedFileMatch() throws Exception {
        List<City> cities = loadSeedCities();
        assertThat(cities).hasSize(30);

        WeightedGraph graph = WeightedGraph.completeGraph(cities);
        graph.checkInvariants();

        MstResult prim = new PrimMST().primMST(graph);
        MstResult kruskal = new KruskalMST().kruskalMST(graph);

        // 30 城完全图：30*29/2 = 435 条边，MST 29 条边
        assertThat(graph.vertexCount()).isEqualTo(30);
        assertThat(graph.edgeCount()).isEqualTo(435);
        assertThat(prim.getEdgeCount()).isEqualTo(29);
        assertThat(kruskal.getEdgeCount()).isEqualTo(29);
        assertThat(prim.isConnected()).isTrue();
        assertThat(kruskal.isConnected()).isTrue();
        assertThat(prim.getTotalWeight())
            .isCloseTo(kruskal.getTotalWeight(), within(TOLERANCE));

        // 总权值应等于选中边权值之和，排除「只加了边没汇总」的错误
        assertThat(prim.getTotalWeight())
            .isCloseTo(sumWeights(prim), within(TOLERANCE));
        assertThat(kruskal.getTotalWeight())
            .isCloseTo(sumWeights(kruskal), within(TOLERANCE));

        assertThat(prim.getEdges())
            .allMatch(edge -> edge.getStartId() < edge.getEndId());
    }

    @Test
    void emptyGraphReturnsEmptyMst() {
        WeightedGraph graph = new WeightedGraph();

        MstResult prim = new PrimMST().primMST(graph);
        MstResult kruskal = new KruskalMST().kruskalMST(graph);

        assertThat(prim.getNodeCount()).isZero();
        assertThat(prim.getEdgeCount()).isZero();
        assertThat(prim.getTotalWeight()).isZero();
        assertThat(prim.isConnected()).isFalse();

        assertThat(kruskal.getNodeCount()).isZero();
        assertThat(kruskal.getEdgeCount()).isZero();
        assertThat(kruskal.getTotalWeight()).isZero();
        assertThat(kruskal.isConnected()).isFalse();
    }

    @Test
    void singleVertexIsConnectedWithNoEdge() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex(1000);

        MstResult prim = new PrimMST().primMST(graph);
        MstResult kruskal = new KruskalMST().kruskalMST(graph);

        assertThat(prim.getNodeCount()).isEqualTo(1);
        assertThat(prim.getEdgeCount()).isZero();
        assertThat(prim.getTotalWeight()).isZero();
        assertThat(prim.isConnected()).isTrue();

        assertThat(kruskal.getEdgeCount()).isZero();
        assertThat(kruskal.getTotalWeight()).isZero();
        assertThat(kruskal.isConnected()).isTrue();
    }

    @Test
    void disconnectedGraphReturnsForest() {
        WeightedGraph graph = new WeightedGraph();
        for (int id = 1; id <= 4; id++) {
            graph.addVertex(id);
        }
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(3, 4, 2.0);

        MstResult prim = new PrimMST().primMST(graph);
        MstResult kruskal = new KruskalMST().kruskalMST(graph);

        // 4 个点、2 个连通分量 -> 最小生成森林 2 条边，不是 n-1
        assertThat(prim.isConnected()).isFalse();
        assertThat(prim.getEdgeCount()).isEqualTo(2);
        assertThat(prim.getTotalWeight()).isCloseTo(3.0, within(TOLERANCE));

        assertThat(kruskal.isConnected()).isFalse();
        assertThat(kruskal.getEdgeCount()).isEqualTo(2);
        assertThat(kruskal.getTotalWeight()).isCloseTo(3.0, within(TOLERANCE));
    }

    @Test
    void equalWeightsProduceSameDeterministicEdges() {
        WeightedGraph graph = new WeightedGraph();
        for (int id = 1000; id <= 1003; id++) {
            graph.addVertex(id);
        }
        // 4 城完全图，6 条边全部等权；tie-break 必须让两种算法选出同一组边
        for (int i = 1000; i <= 1003; i++) {
            for (int j = i + 1; j <= 1003; j++) {
                graph.addEdge(i, j, 1.0);
            }
        }

        MstResult prim = new PrimMST().primMST(graph);
        MstResult kruskal = new KruskalMST().kruskalMST(graph);

        assertThat(prim.getTotalWeight()).isCloseTo(3.0, within(TOLERANCE));
        assertThat(kruskal.getTotalWeight()).isCloseTo(3.0, within(TOLERANCE));
        assertThat(edgeKeys(prim)).containsExactly(
            "1000-1001", "1000-1002", "1000-1003");
        assertThat(edgeKeys(kruskal)).containsExactlyElementsOf(edgeKeys(prim));
    }

    @Test
    void unionFindMergesComponents() {
        UnionFind unionFind = new UnionFind(5);
        assertThat(unionFind.componentCount()).isEqualTo(5);

        assertThat(unionFind.union(0, 1)).isTrue();
        assertThat(unionFind.union(1, 2)).isTrue();
        assertThat(unionFind.connected(0, 2)).isTrue();
        assertThat(unionFind.componentCount()).isEqualTo(3);

        // 已经连通，再合并返回 false，分量数不变
        assertThat(unionFind.union(0, 2)).isFalse();
        assertThat(unionFind.componentCount()).isEqualTo(3);
    }

    private static List<City> eightCities() {
        return List.of(
            new City(1000, "北京", 116.41, 39.90),
            new City(1001, "上海", 121.47, 31.23),
            new City(1002, "广州", 113.26, 23.13),
            new City(1003, "成都", 104.07, 30.67),
            new City(1004, "西安", 108.95, 34.27),
            new City(1005, "武汉", 114.31, 30.52),
            new City(1006, "杭州", 120.19, 30.26),
            new City(1007, "沈阳", 123.43, 41.80));
    }

    /**
     * 从主资源目录读取 30 城种子文件，规则和文档 5.4 / 附录 C 一致：
     * 跳过空行和 # 注释，一行三列，id = 1000 + 有效行号。
     */
    private static List<City> loadSeedCities() throws Exception {
        List<City> cities = new ArrayList<>();
        try (InputStream input = MstTest.class.getResourceAsStream("/data/cities.txt")) {
            assertThat(input).as("找不到 classpath:/data/cities.txt").isNotNull();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                int id = 1000;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\s+");
                    assertThat(parts).as("数据行格式应为 name longitude latitude").hasSize(3);
                    cities.add(new City(id++, parts[0],
                        Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                }
            }
        }
        return cities;
    }

    private static List<String> edgeKeys(MstResult result) {
        return result.getEdges().stream()
            .map(edge -> edge.getStartId() + "-" + edge.getEndId())
            .toList();
    }

    private static double sumWeights(MstResult result) {
        double sum = 0.0;
        for (Edge edge : result.getEdges()) {
            sum += edge.getWeight();
        }
        return Math.round(sum * 100) / 100.0;
    }
}

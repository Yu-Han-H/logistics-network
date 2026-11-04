package com.logistics.algorithm.path;

import com.logistics.model.City;
import com.logistics.structure.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Dijkstra 最短路径的单元测试。
 */
class DijkstraShortestPathTest {

    private static final double TOLERANCE = 0.005;

    @Test
    void eightCitiesDirectPathMatchesDocumentExample() {
        WeightedGraph graph = WeightedGraph.completeGraph(eightCities());
        DijkstraShortestPath dijkstra = new DijkstraShortestPath();

        DijkstraShortestPath.PathResult guangzhouToBeijing =
            dijkstra.shortestPath(graph, 1002, 1000);
        assertThat(guangzhouToBeijing.isFound()).isTrue();
        assertThat(guangzhouToBeijing.getCost()).isCloseTo(1890.62, within());
        assertThat(guangzhouToBeijing.getHops()).isEqualTo(1);
        assertThat(guangzhouToBeijing.getPath()).containsExactly(1002, 1000);

        DijkstraShortestPath.PathResult beijingToShanghai =
            dijkstra.shortestPath(graph, 1000, 1001);
        assertThat(beijingToShanghai.isFound()).isTrue();
        assertThat(beijingToShanghai.getCost()).isCloseTo(1068.39, within());
        assertThat(beijingToShanghai.getPath()).containsExactly(1000, 1001);
    }

    @Test
    void sameCityReturnsZeroCostPath() {
        WeightedGraph graph = WeightedGraph.completeGraph(eightCities());

        DijkstraShortestPath.PathResult result =
            new DijkstraShortestPath().shortestPath(graph, 1000, 1000);

        assertThat(result.isFound()).isTrue();
        assertThat(result.getCost()).isEqualTo(0.0);
        assertThat(result.getHops()).isZero();
        assertThat(result.getPath()).containsExactly(1000);
    }

    @Test
    void unknownCityIsRejected() {
        WeightedGraph graph = WeightedGraph.completeGraph(eightCities());
        DijkstraShortestPath dijkstra = new DijkstraShortestPath();

        assertThatThrownBy(() -> dijkstra.shortestPath(graph, 9999, 1000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("起点城市不存在");
        assertThatThrownBy(() -> dijkstra.shortestPath(graph, 1000, 9999))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("终点城市不存在");
    }

    @Test
    void unreachableCityReturnsNotFound() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 2, 1.0);

        DijkstraShortestPath.PathResult result =
            new DijkstraShortestPath().shortestPath(graph, 1, 3);

        assertThat(result.isFound()).isFalse();
        assertThat(result.getCost()).isNull();
        assertThat(result.getHops()).isZero();
        assertThat(result.getPath()).isEmpty();
    }

    @Test
    void multiHopPathIsReconstructedInOrder() {
        WeightedGraph graph = new WeightedGraph();
        for (int id = 1; id <= 4; id++) {
            graph.addVertex(id);
        }
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 3, 1.0);
        graph.addEdge(3, 4, 1.0);
        graph.addEdge(1, 4, 10.0);  // 直连更贵，应该走 1 -> 2 -> 3 -> 4

        DijkstraShortestPath.PathResult result =
            new DijkstraShortestPath().shortestPath(graph, 1, 4);

        assertThat(result.isFound()).isTrue();
        assertThat(result.getCost()).isCloseTo(3.0, within());
        assertThat(result.getHops()).isEqualTo(3);
        assertThat(result.getPath()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void equalWeightPathsAreDeterministic() {
        WeightedGraph graph = new WeightedGraph();
        for (int id = 1; id <= 4; id++) {
            graph.addVertex(id);
        }
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(1, 3, 1.0);
        graph.addEdge(2, 4, 1.0);
        graph.addEdge(3, 4, 1.0);

        DijkstraShortestPath.PathResult result =
            new DijkstraShortestPath().shortestPath(graph, 1, 4);

        assertThat(result.getCost()).isCloseTo(2.0, within());
        // 1->2->4 和 1->3->4 等权，优先队列按城市 id 先处理 2，结果固定
        assertThat(result.getPath()).containsExactly(1, 2, 4);
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

    private static org.assertj.core.data.Offset<Double> within() {
        return org.assertj.core.data.Offset.offset(TOLERANCE);
    }
}

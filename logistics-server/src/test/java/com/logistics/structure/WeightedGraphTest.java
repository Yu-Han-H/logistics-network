package com.logistics.structure;

import com.logistics.model.City;
import com.logistics.model.Edge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 邻接表图的单元测试，重点覆盖这次 review 修掉的确定性问题：
 * 边端点规范化、同向重复边、删点无悬挂边、负权更新不破坏图。
 */
class WeightedGraphTest {

    @Test
    void addEdgeNormalizesEndpointsAndStoresBothDirections() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex(1000);
        graph.addVertex(1001);

        // 故意反着传，Edge 应规范成 1000 -> 1001
        graph.addEdge(1001, 1000, 12.34);
        graph.checkInvariants();

        assertThat(graph.edgeCount()).isEqualTo(1);
        assertThat(graph.adjacencyRecordCount()).isEqualTo(2);
        assertThat(graph.hasEdge(1000, 1001)).isTrue();
        assertThat(graph.hasEdge(1001, 1000)).isTrue();
        assertThat(graph.degree(1000)).isEqualTo(1);
        assertThat(graph.degree(1001)).isEqualTo(1);

        Edge edge = graph.edges().get(0);
        assertThat(edge.getStartId()).isEqualTo(1000);
        assertThat(edge.getEndId()).isEqualTo(1001);
        assertThat(edge.getWeight()).isEqualTo(12.34);
    }

    @Test
    void duplicateEdgeIsRejectedEvenIfDirectionIsReversed() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex(1000);
        graph.addVertex(1001);
        graph.addEdge(1000, 1001, 10.0);

        assertThatThrownBy(() -> graph.addEdge(1001, 1000, 10.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("边已存在");

        graph.checkInvariants();
        assertThat(graph.edgeCount()).isEqualTo(1);
    }

    @Test
    void removeVertexRemovesAllIncidentEdges() {
        WeightedGraph graph = WeightedGraph.completeGraph(List.of(
            new City(1000, "北京", 116.41, 39.90),
            new City(1001, "上海", 121.47, 31.23),
            new City(1002, "广州", 113.26, 23.13),
            new City(1003, "成都", 104.07, 30.67)));

        assertThat(graph.removeVertex(1000)).isTrue();
        graph.checkInvariants();

        assertThat(graph.vertexCount()).isEqualTo(3);
        assertThat(graph.edgeCount()).isEqualTo(3);
        assertThat(graph.degree(1000)).isZero();
        assertThat(graph.neighbors(1000)).isEmpty();
        assertThat(graph.hasEdge(1001, 1002)).isTrue();
    }

    @Test
    void updateWeightRejectsNegativeValueWithoutLosingEdge() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex(1000);
        graph.addVertex(1001);
        graph.addEdge(1000, 1001, 10.0);

        assertThatThrownBy(() -> graph.updateWeight(1000, 1001, -1.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不能为负");

        // 先删旧边再校验负权，会把边弄丢；这里断言边还在
        graph.checkInvariants();
        assertThat(graph.hasEdge(1000, 1001)).isTrue();
        assertThat(graph.neighbors(1000).get(0).getWeight()).isEqualTo(10.0);
    }

    @Test
    void removeEdgeRejectsSelfLoopAndHandlesMissingEdge() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex(1000);
        graph.addVertex(1001);
        graph.addEdge(1000, 1001, 10.0);

        assertThat(graph.removeEdge(1000, 1000)).isFalse();
        assertThat(graph.removeEdge(1000, 9999)).isFalse();
        graph.checkInvariants();

        assertThat(graph.removeEdge(1001, 1000)).isTrue();
        graph.checkInvariants();
        assertThat(graph.edgeCount()).isZero();
        assertThat(graph.adjacencyRecordCount()).isZero();
    }

    @Test
    void completeGraphRejectsDuplicateCityId() {
        List<City> cities = List.of(
            new City(1000, "北京", 116.41, 39.90),
            new City(1000, "北京副本", 116.41, 39.90));

        assertThatThrownBy(() -> WeightedGraph.completeGraph(cities))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("城市 id 重复");
    }
}

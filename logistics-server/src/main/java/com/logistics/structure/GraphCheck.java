package com.logistics.structure;

import com.logistics.model.City;
import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;

import java.util.List;

/**
 * 邻接表图的自检程序（不依赖 JUnit）。
 *
 * <p>用法：右键 Run 'GraphCheck.main()'。
 * 全部通过时最后一行是「通过 N 项，失败 0 项」，退出码 0；
 * 有失败项时打印 [FAIL] 并返回退出码 1。
 */
public class GraphCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        System.out.println("=== 邻接表图自检 ===");

        WeightedGraph graph = WeightedGraph.completeGraph(eightCities());

        // ---------- 1. 结构 ----------
        check("顶点数 = 8", graph.vertexCount() == 8);
        check("无向边数 = 28", graph.edgeCount() == 28);
        check("邻接记录数 = 56", records(graph) == 56);
        check("北京（1000）的度 = 7", graph.degree(1000) == 7);
        check("沈阳（1007）的度 = 7", graph.degree(1007) == 7);

        // ---------- 2. 权值 ----------
        check("北京—上海 = 1068.39", closeTo(weightOf(graph, 1000, 1001), 1068.39));
        check("北京—沈阳 = 627.82", closeTo(weightOf(graph, 1000, 1007), 627.82));
        check("北京—西安 = 911.96", closeTo(weightOf(graph, 1000, 1004), 911.96));

        // ---------- 3. 无向性 ----------
        check("每条边两个方向都存在且等权", allSymmetric(graph));
        check("所有边已归一化 fromId < toId",
            graph.edges().stream().allMatch(edge -> edge.getStartId() < edge.getEndId()));
        check("初始不变量校验通过", invariantsOk(graph));

        // ---------- 4. 删除城市 ----------
        WeightedGraph afterRemove = WeightedGraph.completeGraph(eightCities());
        boolean removed = afterRemove.removeVertex(1000);
        check("removeVertex(1000) 返回 true", removed);
        check("删除后顶点数 = 7", afterRemove.vertexCount() == 7);
        check("删除后边数 = 21", afterRemove.edgeCount() == 21);
        check("北京没有残留邻居", afterRemove.neighbors(1000).isEmpty());
        check("删除后不变量校验通过", invariantsOk(afterRemove));

        // ---------- 5. 更新边权 ----------
        WeightedGraph afterUpdate = WeightedGraph.completeGraph(eightCities());
        afterUpdate.updateWeight(1000, 1001, 1000.00);
        check("更新权值后边数仍为 28", afterUpdate.edgeCount() == 28);
        check("更新后北京—上海 = 1000.00", closeTo(weightOf(afterUpdate, 1000, 1001), 1000.00));
        check("更新后不变量校验通过", invariantsOk(afterUpdate));

        // ---------- 6. 重复加边 ----------
        boolean threw = false;
        try {
            afterUpdate.addEdge(1000, 1001, 1.00);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("重复加边抛 IllegalArgumentException", threw);

        // ---------- 汇总 ----------
        System.out.println("----------------------------------------");
        System.out.printf("通过 %d 项，失败 %d 项%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // 断言工具
    // ------------------------------------------------------------------

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static boolean closeTo(double actual, double expected) {
        return Math.abs(actual - expected) < 0.005;
    }

    private static boolean invariantsOk(WeightedGraph graph) {
        try {
            graph.checkInvariants();
            return true;
        } catch (IllegalStateException e) {
            System.out.println("       " + e.getMessage());
            return false;
        }
    }

    private static int records(WeightedGraph graph) {
        return graph.vertices().stream().mapToInt(graph::degree).sum();
    }

    private static boolean allSymmetric(WeightedGraph graph) {
        for (Edge edge : graph.edges()) {
            if (!graph.hasEdge(edge.getStartId(), edge.getEndId())) {
                return false;
            }
            if (!closeTo(weightOf(graph, edge.getStartId(), edge.getEndId()),
                weightOf(graph, edge.getStartId(), edge.getEndId()))) {
                return false;
            }
        }
        return true;
    }

    private static double weightOf(WeightedGraph graph, int from, int to) {
        return graph.neighbors(from).stream()
            .filter(edge -> edge.connects(from, to))
            .findFirst()
            .orElseThrow()
            .getWeight();
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
}

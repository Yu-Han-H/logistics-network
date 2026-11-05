package com.logistics.algorithm.path;

import com.logistics.model.City;
import com.logistics.structure.WeightedGraph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Dijkstra 最短路自检程序（不依赖 JUnit）。
 *
 * <p>用法：右键 Run 'PathCheck.main()'。
 * 全部通过时最后一行是「通过 N 项，失败 0 项」，退出码 0；
 * 有失败项时打印 [FAIL] 并返回退出码 1。答辩时可以现场跑。
 */
public class PathCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        System.out.println("=== Dijkstra 最短路自检 ===");

        WeightedGraph graph = WeightedGraph.completeGraph(eightCities());
        DijkstraShortestPath dijkstra = new DijkstraShortestPath(graph);

        // ---------- 1. 已知城市对 ----------
        PathResult guangzhouToBeijing = dijkstra.shortestPath(1002, 1000);
        check("广州→北京 找到路径", guangzhouToBeijing.isFound());
        check("广州→北京 cost = 1890.62", closeTo(guangzhouToBeijing.getCost(), 1890.62));
        check("广州→北京 hops = 1", guangzhouToBeijing.getHops() == 1);
        check("广州→北京 路径 = [1002, 1000]",
            guangzhouToBeijing.getPath().equals(List.of(1002, 1000)));

        PathResult beijingToShanghai = dijkstra.shortestPath(1000, 1001);
        check("北京→上海 cost = 1068.39", closeTo(beijingToShanghai.getCost(), 1068.39));

        PathResult beijingToShenyang = dijkstra.shortestPath(1000, 1007);
        check("北京→沈阳 cost = 627.82", closeTo(beijingToShenyang.getCost(), 627.82));

        // ---------- 2. 边界 ----------
        PathResult sameCity = dijkstra.shortestPath(1000, 1000);
        check("起点=终点 cost = 0", closeTo(sameCity.getCost(), 0.0));
        check("起点=终点 hops = 0", sameCity.getHops() == 0);
        check("起点=终点 路径只有一个城市", sameCity.getPath().equals(List.of(1000)));

        boolean threw = false;
        try {
            dijkstra.shortestPath(9999, 1000);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("城市不存在抛 IllegalArgumentException", threw);

        WeightedGraph disconnected = new WeightedGraph();
        disconnected.addVertex(1);
        disconnected.addVertex(2);
        disconnected.addVertex(3);
        disconnected.addEdge(1, 2, 10.0);
        PathResult unreachable = new DijkstraShortestPath(disconnected).shortestPath(1, 3);
        check("不可达 found = false", !unreachable.isFound());
        check("不可达 路径为空", unreachable.getPath().isEmpty());

        // ---------- 3. 任意两城：cost 必须等于路径边权之和 ----------
        boolean allConsistent = true;
        int pairCount = 0;
        for (int from = 1000; from <= 1007; from++) {
            for (int to = 1000; to <= 1007; to++) {
                PathResult result = dijkstra.shortestPath(from, to);
                if (!result.isFound()) {
                    allConsistent = false;
                    continue;
                }
                double sum = 0.0;
                List<Integer> path = result.getPath();
                for (int i = 0; i + 1 < path.size(); i++) {
                    sum += weightOf(graph, path.get(i), path.get(i + 1));
                }
                if (!closeTo(sum, result.getCost())) {
                    allConsistent = false;
                }
                pairCount++;
            }
        }
        check("8 城 " + pairCount + " 组城市对 cost = 路径边权和", allConsistent);

        // ---------- 4. 30 城规模 ----------
        List<City> seed = loadSeedCities();
        if (seed.isEmpty()) {
            System.out.println("[SKIP] 未找到 classpath:/data/cities.txt，跳过 30 城检查");
        } else {
            WeightedGraph bigGraph = WeightedGraph.completeGraph(seed);
            DijkstraShortestPath bigDijkstra = new DijkstraShortestPath(bigGraph);

            boolean allReachable = true;
            int bigPairs = 0;
            for (City from : seed) {
                for (City to : seed) {
                    if (from.getId() == to.getId()) {
                        continue;
                    }
                    PathResult result = bigDijkstra.shortestPath(from.getId(), to.getId());
                    List<Integer> path = result.getPath();
                    if (!result.isFound() || result.getCost() <= 0) {
                        allReachable = false;
                    } else if (path.get(0).intValue() != from.getId()
                        || path.get(path.size() - 1).intValue() != to.getId()) {
                        allReachable = false;
                    }
                    bigPairs++;
                }
            }
            check("30 城 " + bigPairs + " 组城市对全部可达且首尾正确", allReachable);

            PathResult sample = bigDijkstra.shortestPath(seed.get(0).getId(),
                seed.get(seed.size() - 1).getId());
            check("30 城样本路径非空", sample.isFound() && !sample.getPath().isEmpty());
            System.out.println("       样本：" + sample.getPath() + "，cost=" + sample.getCost()
                + "，耗时=" + sample.getElapsedNanos() + " ns");
        }

        // ---------- 汇总 ----------
        System.out.println("----------------------------------------");
        System.out.printf("通过 %d 项，失败 %d 项%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

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
        return Math.abs(actual - expected) <= 0.005;
    }

    /** 直接查图里一条边的权值，用于核对「cost = 路径边权和」。 */
    private static double weightOf(WeightedGraph graph, int a, int b) {
        for (var edge : graph.neighbors(a)) {
            if (edge.connects(a, b)) {
                return edge.getWeight();
            }
        }
        throw new IllegalStateException("边不存在：" + a + " - " + b);
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
    private static List<City> loadSeedCities() {
        List<City> cities = new ArrayList<>();
        try (InputStream input = PathCheck.class.getResourceAsStream("/data/cities.txt")) {
            if (input == null) {
                return List.of();
            }
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
                    if (parts.length != 3) {
                        throw new IllegalStateException("数据行格式应为 name longitude latitude：" + trimmed);
                    }
                    cities.add(new City(id++, parts[0],
                        Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("读取 data/cities.txt 失败", e);
        }
        return cities;
    }
}

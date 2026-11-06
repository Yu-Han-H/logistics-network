package com.logistics.service;

import com.logistics.dto.AdjacencyDTO;
import com.logistics.dto.CityRequest;
import com.logistics.dto.GraphDTO;
import com.logistics.dto.MstDTO;
import com.logistics.dto.MstEdgeDTO;
import com.logistics.dto.PathDTO;
import com.logistics.dto.PathNodeDTO;
import com.logistics.exception.CityNotFoundException;
import com.logistics.model.Edge;
import com.logistics.structure.WeightedGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service 层自检（不依赖 JUnit、不启动 Spring）。
 *
 * <p>用法：右键 Run 'ServiceCheck.main()'。
 * 全部通过时最后一行是「通过 N 项，失败 0 项」，退出码 0；
 * 有失败项时打印 [FAIL] 并返回退出码 1，答辩时可以现场跑。
 */
public class ServiceCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        System.out.println("=== Service 层自检（30 城真实数据）===");

        // 手动组装依赖，效果和 Spring 注入一样
        CityService cityService = new CityService();
        cityService.init();
        GraphService graphService = new GraphService(cityService);
        MstService mstService = new MstService(graphService, cityService);
        PathService pathService = new PathService(graphService, cityService);

        checkCityLoaded(cityService);
        checkGraph(graphService);
        checkAdjacency(graphService);
        MstDTO prim = checkMst(mstService);
        checkPaths(pathService, graphService);
        checkTreeMode(pathService, prim);
        checkBoundaries(pathService, mstService);
        checkCache(cityService, graphService, mstService);

        System.out.println("----------------------------------------");
        System.out.printf("通过 %d 项，失败 %d 项%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // 各项检查
    // ------------------------------------------------------------------

    private static void checkCityLoaded(CityService cityService) {
        check("城市数 = 30", cityService.list().size() == 30);
        check("初始版本 = 0", cityService.version() == 0);
        check("北京是第一个城市", "北京".equals(cityService.list().get(0).name()));
    }

    private static void checkGraph(GraphService graphService) {
        GraphDTO graph = graphService.graph();

        check("图节点数 = 30", graph.nodeCount() == 30);
        check("图边数 = 435", graph.edgeCount() == 435);
        check("节点 id 是字符串 \"1000\"", "1000".equals(graph.nodes().get(0).id()));
        check("边的端点是字符串", graph.edges().get(0).source() instanceof String);
        check("图版本 = 0", graphService.version() == 0);
    }

    private static void checkAdjacency(GraphService graphService) {
        List<AdjacencyDTO> rows = graphService.adjacency();

        check("邻接表 30 行", rows.size() == 30);
        check("北京有 29 个邻居", rows.get(0).neighbors().size() == 29);
        check("北京第一个邻居是上海（1001）", rows.get(0).neighbors().get(0).toId() == 1001);
        check("邻居都带名称", rows.get(0).neighbors().get(0).toName() != null
            && !rows.get(0).neighbors().get(0).toName().isBlank());
    }

    private static MstDTO checkMst(MstService mstService) {
        MstDTO prim = mstService.compute("prim");
        MstDTO kruskal = mstService.compute("kruskal");

        check("Prim 边数 = 29", prim.edgeCount() == 29);
        check("Prim 连通", prim.connected());
        check("Kruskal 边数 = 29", kruskal.edgeCount() == 29);
        check("两种算法总权值相等",
            Math.abs(prim.totalWeight() - kruskal.totalWeight()) <= 1e-6);
        check("总权值 > 0", prim.totalWeight() > 0);
        check("MST 边都带城市名", prim.edges().stream()
            .allMatch(edge -> !edge.fromName().isBlank() && !edge.toName().isBlank()));

        System.out.printf("       30 城 MST 总权值 = %.2f km，耗时 = %d ns%n",
            prim.totalWeight(), prim.elapsedNanos());
        return prim;
    }

    private static void checkPaths(PathService pathService, GraphService graphService) {
        PathDTO dto = pathService.shortest(1002, 1000, "graph");

        check("广州→北京 找到路径", dto.found());
        check("广州→北京 首尾正确", dto.path().get(0).id() == 1002
            && dto.path().get(dto.path().size() - 1).id() == 1000);
        check("广州→北京 cost > 0", dto.cost() != null && dto.cost() > 0);
        check("广州→北京 hops = 路径边数", dto.hops() == dto.path().size() - 1);
        check("cost = 路径边权之和",
            Math.abs(dto.cost() - pathWeight(graphService.currentGraph(), dto.path())) <= 0.01);

        System.out.printf("       广州→北京 cost = %.2f km，hops = %d，path = %s%n",
            dto.cost(), dto.hops(), pathNames(dto));

        PathDTO same = pathService.shortest(1000, 1000, "graph");
        check("起点=终点 cost = 0", same.cost() != null && same.cost() == 0.0);
        check("起点=终点 hops = 0", same.hops() == 0);
        check("起点=终点 path 只有一个城市", same.path().size() == 1);
    }

    private static void checkTreeMode(PathService pathService, MstDTO prim) {
        PathDTO tree = pathService.shortest(1002, 1000, "tree");

        check("tree 模式找到路径", tree.found());
        check("tree 模式标记正确", "tree".equals(tree.mode()));
        check("tree 路径首尾正确", tree.path().get(0).id() == 1002
            && tree.path().get(tree.path().size() - 1).id() == 1000);

        Set<String> mstEdges = new HashSet<>();
        for (MstEdgeDTO edge : prim.edges()) {
            mstEdges.add(edgeKey(edge.fromId(), edge.toId()));
        }
        boolean allOnMst = true;
        for (int i = 0; i + 1 < tree.path().size(); i++) {
            if (!mstEdges.contains(edgeKey(tree.path().get(i).id(), tree.path().get(i + 1).id()))) {
                allOnMst = false;
                break;
            }
        }
        check("tree 路径每条边都在 MST 上", allOnMst);

        System.out.printf("       广州→北京（tree）path = %s%n", pathNames(tree));
    }

    private static void checkBoundaries(PathService pathService, MstService mstService) {
        checkThrows("城市不存在 → CityNotFoundException", CityNotFoundException.class,
            () -> pathService.shortest(9999, 1000, "graph"));

        checkThrows("非法 mode → IllegalArgumentException", IllegalArgumentException.class,
            () -> pathService.shortest(1000, 1001, "car"));

        checkThrows("非法 algorithm → IllegalArgumentException", IllegalArgumentException.class,
            () -> mstService.compute("boruvka"));
    }

    private static void checkCache(CityService cityService, GraphService graphService,
                                   MstService mstService) {
        check("写操作前图节点 = 30", graphService.graph().nodeCount() == 30);

        cityService.add(new CityRequest("自检测试城", 110.0, 30.0));

        check("新增后版本 = 1", cityService.version() == 1);
        check("新增后图节点 = 31", graphService.graph().nodeCount() == 31);
        check("新增后图边数 = 465", graphService.graph().edgeCount() == 465);
        check("新增后 MST 边数 = 30", mstService.compute("prim").edgeCount() == 30);

        cityService.reset();

        check("重置后版本 = 2", cityService.version() == 2);
        check("重置后图节点 = 30", graphService.graph().nodeCount() == 30);
        check("重置后图边数 = 435", graphService.graph().edgeCount() == 435);
    }

    // ------------------------------------------------------------------
    // 断言与工具
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

    private static void checkThrows(String name, Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
            check(name, false);
        } catch (Throwable t) {
            check(name, expected.isInstance(t));
        }
    }

    private static String edgeKey(int a, int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

    private static double pathWeight(WeightedGraph graph, List<PathNodeDTO> path) {
        double sum = 0.0;
        for (int i = 0; i + 1 < path.size(); i++) {
            sum += weightOf(graph, path.get(i).id(), path.get(i + 1).id());
        }
        return sum;
    }

    private static double weightOf(WeightedGraph graph, int a, int b) {
        for (Edge edge : graph.neighbors(a)) {
            if (edge.connects(a, b)) {
                return edge.getWeight();
            }
        }
        throw new IllegalStateException("边不存在：" + a + " - " + b);
    }

    private static String pathNames(PathDTO dto) {
        return dto.path().stream()
            .map((PathNodeDTO node) -> node.name())
            .collect(Collectors.joining(" → ", "[", "]"));
    }
}

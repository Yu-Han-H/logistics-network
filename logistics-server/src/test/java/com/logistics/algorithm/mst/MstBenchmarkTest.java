package com.logistics.algorithm.mst;
import com.logistics.model.City;
import com.logistics.structure.WeightedGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Prim 与 Kruskal 的横向对比测试：同一张图、同一批数据，比较结果、耗时和内存分配。
 *
 * <p>方法参照文档 9.4：
 * <ul>
 *   <li>先预热，让 JIT 编译热点代码；</li>
 *   <li>多次重复，取中位数和 P99，避免单次抖动；</li>
 *   <li>同时覆盖稠密图（完全图）和稀疏图（环形 + 邻边），验证「稠密 Prim 优、稀疏 Kruskal 优」；</li>
 *   <li>用 ThreadMXBean 统计单次调用的分配字节，近似对比空间开销。</li>
 * </ul>
 *
 * <p>注意：不断言「谁更快」，因为结果依赖机器和 JIT；测试只保证两算法结果一致，
 * 并把数据打印出来供报告使用。需要更大规模时运行 {@link #main(String[])}。
 */
class MstBenchmarkTest {

    /** 防止 JIT 把没被使用的计算结果优化掉。 */
    private static volatile long blackhole;

    @Test
    @DisplayName("同一张图上 Prim 与 Kruskal 的总权值、边数、连通性一致")  //给这个测试用例起一个**可读性名字**，只用来在测试报告、IDE（IDEA）测试面板上显示
    void sameGraphSameResult() {
        for (WeightedGraph graph : List.of(complete(30), complete(100), sparse(500, 6))) {
            MstResult prim = new PrimMST().primMST(graph);
            MstResult kruskal = new KruskalMST().kruskalMST(graph);

            assertThat(prim.getTotalWeight())
                .isCloseTo(kruskal.getTotalWeight(), within(1e-6));//不是完全相等，而是允许有误差（0.000001）
            assertThat(prim.getEdgeCount()).isEqualTo(kruskal.getEdgeCount());
            assertThat(prim.isConnected()).isEqualTo(kruskal.isConnected());
            assertThat(prim.getNodeCount()).isEqualTo(graph.vertexCount());
        }
    }

    @Test
    @DisplayName("对比耗时与内存分配（稠密 + 稀疏）")
    void benchmark() {
        printEnvironment();// 打印 JDK 版本、系统、核数，报告要写环境
        printTheory();// 打印理论复杂度，方便对照

        List<Row> rows = new ArrayList<>();
        for (Case testCase : standardCases()) {
            rows.add(runCase(testCase));
        }
        printTables(rows);
    }

    public static void main(String[] args) {
        System.out.println("=== MST 完整基准（规模比单元测试大，手动运行）===");
        printEnvironment();
        printTheory();

        List<Row> rows = new ArrayList<>();
        for (Case testCase : standardCases()) {
            rows.add(runCase(testCase));
        }
        for (Case testCase : largeCases()) {
            rows.add(runCase(testCase));
        }
        printTables(rows);
    }

    // ------------------------------------------------------------------
    // 基准执行
    // ------------------------------------------------------------------

    /*
    Case(type, graph)
   │
   ├─  取数据：n、e、两张算法对象
   ├─  对答案：各跑 1 次，结果必须一致
   ├─  定次数 + 预热：warmup 次
   ├─  正式计时：各跑 reps 次，记数组
   ├─  测内存：各跑 1 次
   └─  打包成 Row 返回
     */
    private static Row runCase(Case testCase) {
        WeightedGraph graph = testCase.graph();
        int n = graph.vertexCount();
        int e = graph.edgeCount();

        PrimMST prim = new PrimMST();
        KruskalMST kruskal = new KruskalMST();

        // 结果一致性：不同规模、不同密度都必须一致，否则比较没有意义
        MstResult primResult = prim.primMST(graph);
        MstResult kruskalResult = kruskal.kruskalMST(graph);
        assertThat(primResult.getTotalWeight())
            .isCloseTo(kruskalResult.getTotalWeight(), within(1e-6));
        assertThat(primResult.getEdgeCount()).isEqualTo(kruskalResult.getEdgeCount());

        int reps = repsFor(n);

        // 预热：不计入样本
        int warmup = Math.max(3, reps / 5);
        for (int i = 0; i < warmup; i++) {
            blackhole += prim.primMST(graph).getEdgeCount();
            blackhole += kruskal.kruskalMST(graph).getEdgeCount();
        }

        long[] primSamples = measure(() -> prim.primMST(graph), reps);
        long[] kruskalSamples = measure(() -> kruskal.kruskalMST(graph), reps);

        long primAlloc = measureAllocation(() -> prim.primMST(graph));
        long kruskalAlloc = measureAllocation(() -> kruskal.kruskalMST(graph));

        return new Row(testCase.type(), n, e,
            medianMs(primSamples), p99Ms(primSamples),
            medianMs(kruskalSamples), p99Ms(kruskalSamples),
            primAlloc, kruskalAlloc);
    }

    private static long[] measure(Supplier<MstResult> task, int reps) {
        long[] samples = new long[reps];
        for (int i = 0; i < reps; i++) {
            long start = System.nanoTime();//nanoTime() 返回纳秒数，单调递增，专门用于计时
            MstResult result = task.get();
            samples[i] = System.nanoTime() - start;//再取一次当前时间，减去开始时间 = 这次耗时，存进数组第 i 格。
            blackhole += result.getEdgeCount();//用一下结果，防止 JIT 发现“没人用”把整个计算优化掉。
        }
        return samples;
    }

    private static long measureAllocation(Supplier<MstResult> task) {
        com.sun.management.ThreadMXBean bean =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!bean.isThreadAllocatedMemorySupported()) {
            return -1;
        }
        long before = bean.getCurrentThreadAllocatedBytes();
        MstResult result = task.get();
        long after = bean.getCurrentThreadAllocatedBytes();
        blackhole += result.getEdgeCount();
        return after - before;
    }

    /** 规模越大重复次数越少，保证整套测试几秒内跑完。 */
    private static int repsFor(int n) {
        if (n <= 50) {
            return 500;
        }
        if (n <= 200) {
            return 100;
        }
        if (n <= 400) {
            return 20;
        }
        return 5;
    }

    private static double medianMs(long[] samples) {
        long[] copy = samples.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2] / 1_000_000.0;
    }

    private static double p99Ms(long[] samples) {
        long[] copy = samples.clone();
        Arrays.sort(copy);
        int index = (int) Math.ceil(copy.length * 0.99) - 1;
        index = Math.max(0, Math.min(copy.length - 1, index));
        return copy[index] / 1_000_000.0;
    }

    // ------------------------------------------------------------------
    // 数据准备与输出
    // ------------------------------------------------------------------

    private static List<Case> standardCases() {
        return List.of(
            new Case("稠密", complete(30)),
            new Case("稠密", complete(100)),
            new Case("稠密", complete(300)),
            new Case("稀疏", sparse(200, 6)),
            new Case("稀疏", sparse(1000, 6)));
    }

    private static List<Case> largeCases() {
        return List.of(
            new Case("稠密", complete(600)),
            new Case("稀疏", sparse(2000, 6)),
            new Case("稀疏", sparse(5000, 6)));
    }

    /** 完全图：n 个点、n(n-1)/2 条边，权值由坐标算出来。 */
    private static WeightedGraph complete(int n) {
        List<City> cities = new ArrayList<>(n);
        Random random = new Random(42);
        for (int i = 0; i < n; i++) {
            double longitude = 100 + random.nextDouble() * 20;
            double latitude = 20 + random.nextDouble() * 20;
            cities.add(new City(1000 + i, "C" + i, longitude, latitude));
        }
        return WeightedGraph.completeGraph(cities);
    }

    /**
     * 稀疏图：每个点只和前后各 degree/2 个点相连，形成一个带弦的环，
     * 保证连通且边数约为 n * degree / 2。
     */
    private static WeightedGraph sparse(int n, int degree) {
        WeightedGraph graph = new WeightedGraph();
        for (int i = 0; i < n; i++) {
            graph.addVertex(1000 + i);
        }
        Random random = new Random(7);
        for (int i = 0; i < n; i++) {
            for (int k = 1; k <= degree / 2; k++) {
                int j = (i + k) % n;
                if (i == j) {
                    continue;
                }
                int a = 1000 + Math.min(i, j);
                int b = 1000 + Math.max(i, j);
                if (!graph.hasEdge(a, b)) {
                    graph.addEdge(a, b, 1 + random.nextInt(1000));
                }
            }
        }
        return graph;
    }

    private static void printEnvironment() {
        System.out.println();
        System.out.printf("环境：JDK %s，%s，%d 核%n",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            Runtime.getRuntime().availableProcessors());
    }

    private static void printTheory() {
        System.out.println("理论：Prim O(V²) 时间 / O(V) 空间；Kruskal O(E log E) 时间 / O(V+E) 空间");
        System.out.println();
    }

    private static void printTables(List<Row> rows) {
        System.out.println("==== 耗时对比（单位 ms，中位数 / P99）====");
        System.out.printf("%-4s %6s %9s %12s %12s %14s %14s %8s%n",
            "场景", "n", "E", "Prim中位", "PrimP99", "Kruskal中位", "KruskalP99", "K/P");
        for (Row row : rows) {
            System.out.printf("%-4s %6d %9d %12.3f %12.3f %14.3f %14.3f %7.2fx%n",
                row.type(), row.n(), row.e(),
                row.primMedianMs(), row.primP99Ms(),
                row.kruskalMedianMs(), row.kruskalP99Ms(),
                row.primMedianMs() == 0 ? 0 : row.kruskalMedianMs() / row.primMedianMs());
        }

        System.out.println();
        System.out.println("==== 单次调用分配字节（KB）====");
        System.out.printf("%-4s %6s %9s %12s %14s %8s%n",
            "场景", "n", "E", "Prim分配", "Kruskal分配", "K/P");
        for (Row row : rows) {
            double p = row.primAllocBytes() < 0 ? -1 : row.primAllocBytes() / 1024.0;
            double k = row.kruskalAllocBytes() < 0 ? -1 : row.kruskalAllocBytes() / 1024.0;
            System.out.printf("%-4s %6d %9d %12.1f %14.1f %7.2fx%n",
                row.type(), row.n(), row.e(), p, k, p <= 0 ? 0 : k / p);
        }
    }

    private record Case(String type, WeightedGraph graph) {
    }//record是把两个形参绑定在一起

    private record Row(String type, int n, int e,
                       double primMedianMs, double primP99Ms,
                       double kruskalMedianMs, double kruskalP99Ms,
                       long primAllocBytes, long kruskalAllocBytes) {
    }
}

package com.logistics.algorithm.mst;

/**
 * 并查集（不相交集合），供 Kruskal 判断「新加入的边会不会成环」。
 *
 * <p>实现采用两种优化：
 *   路径压缩：{@link #find(int)} 把查找路径上的节点直接挂到根上，后续查找接近 O(1)。
 *   按秩合并：{@link #union(int, int)} 把较矮的树挂到较高的树下，避免树退化成链。
 *
 * 并查集只认 0..size-1 的下标，不认城市 id。调用方先用
 * {@code cityId -> index} 的映射把城市 id 转成下标，这样算法本身不依赖
 * 城市 id 是否连续。
 *
 * 时间复杂度：一次 find/union 摊还 O(α(n))，α 为反阿克曼函数，
 * 实际可视为常数；空间 O(n)。
 */
public class UnionFind {

    /** parent[i] 表示节点 i 的父节点；根节点的 parent[i] == i。 */
    private final int[] parent;

    /** rank[i] 表示以 i 为根的树的高度上界，只在根节点上有意义。 */
    private final int[] rank;

    /** 当前连通分量个数。初始为 size，每成功合并一次减 1。 */
    private int componentCount;

    /**
     * 创建一个包含 {@code size} 个独立节点的并查集。
     *
     * @param size 元素个数，必须非负
     * @throws IllegalArgumentException size 为负数时抛出
     */
    public UnionFind(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("并查集元素个数不能为负：" + size);
        }
        this.parent = new int[size];
        this.rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
        this.componentCount = size;
    }

    /**
     * 查找元素所在集合的根，同时做路径压缩。
     *
     * @param element 元素下标
     * @return 根节点下标
     * @throws IllegalArgumentException 下标越界时抛出
     */
    public int find(int element) {
        checkElement(element);

        // 第一遍：找到根
        int root = element;
        while (parent[root] != root) {
            root = parent[root];
        }

        // 第二遍：路径压缩，把沿途节点全部直接挂到根上
        while (parent[element] != root) {
            int next = parent[element];
            parent[element] = root;
            element = next;
        }
        return root;
    }

    /**
     * 合并两个元素所在的集合。
     *
     * @param a 元素 a 的下标
     * @param b 元素 b 的下标
     * @return 两个元素原本不在同一集合、本次合并成功返回 true；
     *         已经连通、无需合并返回 false
     */
    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }

        // 按秩合并：让 rank 小的树挂到 rank 大的树下
        if (rank[rootA] < rank[rootB]) {
            int temp = rootA;
            rootA = rootB;
            rootB = temp;
        }
        parent[rootB] = rootA;
        if (rank[rootA] == rank[rootB]) {
            rank[rootA]++;
        }

        componentCount--;
        return true;
    }

    /**
     * 判断两个元素是否属于同一连通分量。
     */
    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /**
     * 当前连通分量个数。Kruskal 用它判断整张图是否连通：
     * 分量为 1 表示连通，大于 1 表示得到的是最小生成森林。
     */
    public int componentCount() {
        return componentCount;
    }

    private void checkElement(int element) {
        if (element < 0 || element >= parent.length) {
            throw new IllegalArgumentException(
                "并查集下标越界：" + element + "，合法范围 0.." + (parent.length - 1));
        }
    }
}

package com.logistics.algorithm.path;

import java.util.List;

/**
 * 最短路径的计算结果。
 *
 * <p>算法层只负责算，不关心城市名和 JSON 字段；Service 层再把 id 补成名称、
 * 转成 PathDTO。字段含义：
 * <ul>
 *   <li>{@code found}：是否可达。完全图下恒为 true，代码仍处理不连通的情况；</li>
 *   <li>{@code fromId}/{@code toId}：起点、终点城市 id；</li>
 *   <li>{@code cost}：总代价，等于路径上各边权值之和，保留 2 位小数；
 *       不可达时为 0，由 Service 转成 null（文档 7.8）；</li>
 *   <li>{@code path}：完整路径的城市 id，从起点到终点；不可达时为空；</li>
 *   <li>{@code hops}：路径边数，直连为 1，{@code from == to} 为 0；</li>
 *   <li>{@code elapsedNanos}：算法耗时，单位纳秒。</li>
 * </ul>
 */
public class PathResult {

    private final boolean found;
    private final int fromId;
    private final int toId;
    private final double cost;
    private final List<Integer> path;
    private final int hops;
    private final long elapsedNanos;

    /**
     * 构造最短路径结果。
     *
     * @param found        是否可达
     * @param fromId       起点城市 id
     * @param toId         终点城市 id
     * @param cost         总代价，不可达时传 0
     * @param path         完整路径，可达时首元素必须是起点、末元素必须是终点
     * @param elapsedNanos 算法耗时
     * @throws IllegalArgumentException 参数自相矛盾时抛出
     */
    public PathResult(boolean found, int fromId, int toId, double cost,
                      List<Integer> path, long elapsedNanos) {
        if (path == null) {
            throw new IllegalArgumentException("路径不能为 null");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("代价不能为负：" + cost);
        }
        if (found) {
            if (path.isEmpty()) {
                throw new IllegalArgumentException("可达时路径不能为空");
            }
            if (path.get(0).intValue() != fromId) {
                throw new IllegalArgumentException("路径首元素必须是起点：" + fromId);
            }
            if (path.get(path.size() - 1).intValue() != toId) {
                throw new IllegalArgumentException("路径末元素必须是终点：" + toId);
            }
        } else if (!path.isEmpty()) {
            throw new IllegalArgumentException("不可达时路径必须为空");
        }

        this.found = found;
        this.fromId = fromId;
        this.toId = toId;
        this.cost = cost;
        this.path = List.copyOf(path);
        this.hops = found ? path.size() - 1 : 0;
        this.elapsedNanos = elapsedNanos;
    }

    public boolean isFound() {
        return found;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    /** 总代价，单位 km，已保留 2 位；不可达时为 0。 */
    public double getCost() {
        return cost;
    }

    /** 完整路径，只读；不可达时为空列表。 */
    public List<Integer> getPath() {
        return path;
    }

    /** 边数：直连为 1，{@code from == to} 为 0。 */
    public int getHops() {
        return hops;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    @Override
    public String toString() {
        return "PathResult{found=" + found
            + ", fromId=" + fromId
            + ", toId=" + toId
            + ", cost=" + cost
            + ", hops=" + hops
            + ", path=" + path
            + ", elapsedNanos=" + elapsedNanos
            + '}';
    }
}

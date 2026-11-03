package com.logistics.model;

import java.util.Objects;

/**
 * 带权无向边数据模型。
 *
 * <p>不变量：
 * <ul>
 *   <li>两端点 id 不能相同（不允许自环）；</li>
 *   <li>{@code startId < endId}，即同一条无向边只有一种规范写法；</li>
 *   <li>权值非负，单位 km，保留 2 位小数。</li>
 * </ul>
 *
 * <p>构造时自动把端点排成 {@code startId < endId}，所以
 * {@code new Edge(1001, 1000, w)} 和 {@code new Edge(1000, 1001, w)}
 * 表示同一条边，{@link #equals(Object)} 和 {@link #hashCode()} 也按规范后的端点比较。
 * 这样邻接表两个方向可以共用同一个 {@link Edge} 对象，权值天然相等。
 *
 * <p>边对象创建后不可修改；坐标变化时由 {@code WeightedGraph.updateWeight}
 * 删除旧边、重建新边。
 */
public class Edge {

    private final int startId;
    private final int endId;
    private final double weight;

    /**
     * 创建一条无向边。
     *
     * @param startId 端点一的城市 id
     * @param endId   端点二的城市 id
     * @param weight  边权，单位 km，不能为负
     * @throws IllegalArgumentException 两端点相同或权值为负时抛出
     */
    public Edge(int startId, int endId, double weight) {
        if (startId == endId) {
            throw new IllegalArgumentException("边的两端点不能相同：" + startId);
        }
        if (weight < 0) {
            throw new IllegalArgumentException("边权不能为负：" + weight);
        }
        // 无向边统一规范为 startId < endId，对应文档 4.4「边的规范化」
        this.startId = Math.min(startId, endId);
        this.endId = Math.max(startId, endId);
        this.weight = weight;
    }

    public int getStartId() {
        return startId;
    }

    public int getEndId() {
        return endId;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * 已知边的一端，返回另一端的城市 id。
     *
     * @param cityId 边上的某一端
     * @return 另一端的城市 id
     * @throws IllegalArgumentException cityId 不在这条边上时抛出
     */
    public int other(int cityId) {
        if (cityId == startId) {
            return endId;
        }
        if (cityId == endId) {
            return startId;
        }
        throw new IllegalArgumentException("城市" + cityId + "不在这条边上：" + this);
    }

    /**
     * 判断这条边是否连接 a、b 两个城市；a、b 的顺序无关。
     */
    public boolean connects(int a, int b) {
        return (a == startId && b == endId) || (a == endId && b == startId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Edge other)) {
            return false;
        }
        return startId == other.startId && endId == other.endId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startId, endId);
    }

    @Override
    public String toString() {
        return "Edge{" + startId + "," + endId + "," + weight + "}";
    }
}

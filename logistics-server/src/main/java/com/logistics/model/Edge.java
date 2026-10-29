package com.logistics.model;

/**
 * 带权无向边数据模型
 * Edge
 */

public class Edge {
    private int startId;
    private int endId;
    // 两点间带权路径，保留两位小数
    private double weight;

    public Edge() {
    }

    public Edge(int startId, int endId, double weight) {
        this.startId = startId;
        this.endId = endId;
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
}

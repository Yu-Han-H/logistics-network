package com.logistics.model;

import java.util.Objects;

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
    //有参构造函数，同时进行初步判断
    public Edge(int startId, int endId, double weight) {
        if(startId == endId){
            throw new IllegalArgumentException("边的两端点不能相同："+startId);
        }
        if(weight<0){
            throw new IllegalArgumentException("边权不能为负："+weight);
        }
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

    //方法--给一端，返回另一端的城市id
    public int other(int cityId){
        if(cityId == startId){
            return endId;
        }
        if(cityId == endId){
            return startId;
        }
        throw new IllegalArgumentException("城市"+cityId+"不在这条边上："+this);
    }

    public boolean connects(int a,int b){
        return (a==startId&&b==endId)||(a==endId&&b==startId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge other) )return false;
        return startId==other.startId && endId==other.endId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(startId, endId);
    }

    @Override
    public String toString() {
        return "Edge{"+startId+","+endId+","+weight+"}";
    }
}

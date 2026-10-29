package com.logistics.model;

/**
 * 城市数据模型
 * City
 */

public class City {
    private int id;
    private String name;
    // 经度
    private double longitude;
    // 纬度
    private double latitude;

    // 无参数构造方法
    public City() {
    }

    // 全参构造
    public City(int id, String name, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // 后续有关 City 的方法写在下面
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    //重写equals--通过id判断是否是相同的城市
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof City other)) {
            return false;
        }
        return id == other.id;
    }

    //重写hashCode--为了满足equals必须要重写hashCode
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    //重写toString--让日志和报错更易理解
    @Override
    public String toString() {
        return "City{id=" + id + ", name='" + name
            + "', longitude=" + longitude + ", latitude=" + latitude + "}";
    }
}

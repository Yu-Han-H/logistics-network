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
}

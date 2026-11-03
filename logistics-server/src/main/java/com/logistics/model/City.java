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

    /**
     * 修改城市名称。id 是主键，不提供 setter，避免改 id 破坏链表、图和缓存。
     *
     * <p>名称唯一性、长度等业务校验放在 Service / DTO，模型只负责存值。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 修改经度。修改后要调 {@code DoublyCircularLinkedList.sortByDistance}
     * 重排链表，并重建相关边权（文档 4.7）。
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * 修改纬度。修改后要调 {@code DoublyCircularLinkedList.sortByDistance}
     * 重排链表，并重建相关边权（文档 4.7）。
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
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

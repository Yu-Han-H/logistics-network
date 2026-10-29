package com.logistics.structure;

import com.logistics.model.City;

public class CityNode {
    City city;
    CityNode prev;
    CityNode next;

    public CityNode(City city) {
        this.city = city;
    }

    public City getCity() {
        return city;
    }

    public CityNode getPrev() {
        return prev;
    }

    public CityNode getNext() {
        return next;
    }

    @Override
    public String toString() {
        return city==null?"null":city.getName();
    }
}

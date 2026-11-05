package com.logistics.dto;

import com.logistics.model.City;

/**
 * 对外城市对象。只放前端需要的字段，不带链表指针。
 */
public record CityDTO(int id, String name, double longitude, double latitude) {

    public static CityDTO from(City city) {
        return new CityDTO(city.getId(), city.getName(), city.getLongitude(), city.getLatitude());
    }
}

package com.logistics.util;

import com.logistics.model.City;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * 距离模型测试，对应《开发文档 v1.1》4.2 的已知值。
 */
class DistanceUtilTest {

    private static City city(int id, String name, double lon, double lat) {
        return new City(id, name, lon, lat);
    }

    private static final City BEIJING = city(1000, "北京", 116.41, 39.90);
    private static final City SHANGHAI = city(1001, "上海", 121.47, 31.23);
    private static final City SHENYANG = city(1007, "沈阳", 123.43, 41.80);
    private static final City XIAN = city(1004, "西安", 108.95, 34.27);
    private static final City GUANGZHOU = city(1002, "广州", 113.26, 23.13);
    private static final City WUHAN = city(1005, "武汉", 114.31, 30.52);
    private static final City HANGZHOU = city(1006, "杭州", 120.19, 30.26);
    private static final City CHENGDU = city(1003, "成都", 104.07, 30.67);

    @Test
    @DisplayName("文档 4.2 的 7 组已知值")
    void knownValues() {
        assertThat(DistanceUtil.distance(BEIJING, SHANGHAI)).isCloseTo(1068.39, within(0.005));
        assertThat(DistanceUtil.distance(BEIJING, SHENYANG)).isCloseTo(627.82, within(0.005));
        assertThat(DistanceUtil.distance(BEIJING, XIAN)).isCloseTo(911.96, within(0.005));
        assertThat(DistanceUtil.distance(GUANGZHOU, WUHAN)).isCloseTo(829.24, within(0.005));
        assertThat(DistanceUtil.distance(SHANGHAI, HANGZHOU)).isCloseTo(163.27, within(0.005));
        assertThat(DistanceUtil.distance(CHENGDU, XIAN)).isCloseTo(608.82, within(0.005));
        assertThat(DistanceUtil.distance(WUHAN, HANGZHOU)).isCloseTo(565.37, within(0.005));
    }

    @Test
    @DisplayName("距离对称：w(A,B) == w(B,A)")
    void symmetric() {
        City[] cities = {BEIJING, SHANGHAI, SHENYANG, XIAN, GUANGZHOU, WUHAN, HANGZHOU, CHENGDU};
        for (City a : cities) {
            for (City b : cities) {
                assertThat(DistanceUtil.distance(a, b)).isEqualTo(DistanceUtil.distance(b, a));
            }
        }
    }

    @Test
    @DisplayName("同一城市距离为 0")
    void sameCityIsZero() {
        assertThat(DistanceUtil.distance(BEIJING, BEIJING)).isZero();
    }

    @Test
    @DisplayName("坐标重载与 City 重载结果一致")
    void coordinateOverloadMatches() {
        double fromCity = DistanceUtil.distance(BEIJING, SHANGHAI);
        double fromNumbers = DistanceUtil.distance(116.41, 39.90, 121.47, 31.23);
        assertThat(fromCity).isEqualTo(fromNumbers);
    }

    @Test
    @DisplayName("结果保留 2 位小数")
    void roundsToTwoDecimals() {
        assertThat(DistanceUtil.round2(1068.3949)).isEqualTo(1068.39);
        assertThat(DistanceUtil.round2(1068.3951)).isEqualTo(1068.4);
    }

    @Test
    @DisplayName("边权求和后整体取整")
    void sumAndRound2() {
        assertThat(DistanceUtil.sumAndRound2(163.27, 565.37, 608.82)).isEqualTo(1337.46);
    }
}

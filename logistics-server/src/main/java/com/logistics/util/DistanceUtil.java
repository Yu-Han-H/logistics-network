package com.logistics.util;

import com.logistics.model.City;

/**
 * 距离模型。实现《开发文档 v1.1》4.2 节，全组唯一的距离计算入口。
 *
 * <p>公式：
 * <pre>
 * dx = (lon2 - lon1) × 111.32 × cos((lat1 + lat2) / 2)
 * dy = (lat2 - lat1) × 111.32
 * d  = sqrt(dx² + dy²)
 * w  = round(d, 2)          单位 km
 * </pre>
 *
 * <p>经度方向用「平均纬度」的余弦，不是起点纬度：用起点纬度会让
 * w(A,B) 与 w(B,A) 不相等，而无向图要求两个方向是同一条边、同一个权值。
 *
 * <p>本类只负责计算，不校验经纬度范围。范围校验放在 CitySaveRequest
 * 和 cities.txt 的加载处，避免读数据时抛异常把启动搞崩。
 */
public final class DistanceUtil {

    /** 每度对应的公里数。全组统一，禁止有人写 111。 */
    public static final double KM_PER_DEGREE = 111.32;

    private DistanceUtil() {
        // 工具类，不允许实例化
    }

    /**
     * 计算两座城市之间的距离。
     *
     * @param from 起点城市，不能为 null
     * @param to   终点城市，不能为 null
     * @return 距离，单位 km，已保留 2 位小数
     */
    public static double distance(City from, City to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("城市不能为 null");
        }
        return distance(from.getLongitude(), from.getLatitude(),
            to.getLongitude(), to.getLatitude());
    }

    /**
     * 按坐标计算距离，方便单测直接传数字。
     *
     * @param lon1 起点经度
     * @param lat1 起点纬度
     * @param lon2 终点经度
     * @param lat2 终点纬度
     * @return 距离，单位 km，已保留 2 位小数
     */
    public static double distance(double lon1, double lat1, double lon2, double lat2) {
        // Math.cos 的参数是弧度，这里必须换算；漏掉 toRadians 结果会差几十倍
        double averageLatitude = Math.toRadians((lat1 + lat2) / 2.0);

        double dx = (lon2 - lon1) * KM_PER_DEGREE * Math.cos(averageLatitude);
        double dy = (lat2 - lat1) * KM_PER_DEGREE;

        // 与 sqrt(dx*dx + dy*dy) 等价，但不会中间溢出
        return round2(Math.hypot(dx, dy));
    }

    /**
     * 四舍五入到 2 位小数。
     *
     * <p>文档 4.2 第 8 条规定就用这个写法。不要用 String.format 转成字符串
     * 再转回 double，那样会引入本地化问题和不必要的解析开销。
     *
     * @param value 原始值
     * @return 保留 2 位小数后的值
     */
    public static double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }

    /**
     * 把若干条已经取整的边权相加，再整体取整一次。
     *
     * <p>文档 4.2 第 5 条：MST 的总权值是所有边「已取整权值」之和，
     * 最后保留 2 位，避免 4360.770000000001 这种浮点尾差。
     *
     * @param weights 已取整的边权
     * @return 总权值，保留 2 位小数
     */
    public static double sumAndRound2(double... weights) {
        double sum = 0.0;
        for (double weight : weights) {
            sum += weight;
        }
        return round2(sum);
    }
}

package com.logistics.service;

import com.logistics.dto.CityDTO;
import com.logistics.dto.CityRefDTO;
import com.logistics.dto.CityRequest;
import com.logistics.dto.SortedCitiesDTO;
import com.logistics.dto.SortedCityDTO;
import com.logistics.exception.CityNotFoundException;
import com.logistics.exception.DuplicateCityException;
import com.logistics.exception.InvalidCoordinateException;
import com.logistics.model.City;
import com.logistics.structure.CityNode;
import com.logistics.structure.DoublyCircularLinkedList;
import com.logistics.util.DistanceUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 城市业务。唯一写入口：增删改城市、维护 graphVersion、维护链表和图的一致性。
 *
 * <p>职责：
 * <ul>
 *   <li>启动时读 resources/data/cities.txt，生成 id = 1000 + 行号；</li>
 *   <li>唯一数据源是 {@link DoublyCircularLinkedList}；</li>
 *   <li>图由 {@link GraphService} 按 {@link #version()} 懒重建，本类不另存一份可写图；</li>
 *   <li>任何写操作（增删改、reset）后 {@code version++}；</li>
 *   <li>校验全部在 Service 层：名称唯一、坐标范围、id 不存在、重名。</li>
 * </ul>
 */

@Service
public class CityService {

    /** 未指定参照城市时的默认值：北京。 */
    private static final int DEFAULT_REFERENCE_ID = 1000;

    /** 种子数据条数（文档 5.4：正好 30 条）。 */
    private static final int SEED_CITY_COUNT = 30;

    /** 城市主数据。reset 时整体替换，所以不是 final。 */
    private DoublyCircularLinkedList cities = new DoublyCircularLinkedList();

    /** 当前排序参照城市，默认北京；参照城市被删时回退到 id 最小的城市。 */
    private int referenceId = DEFAULT_REFERENCE_ID;

    /** 数据版本号：任何写操作后加 1，GraphService/缓存据此失效。 */
    private int version = 0;

    // 优化①：记住下一个可用的 id，只增不减，删除城市后也不复用。
    /** 下一个可用的城市 id；应用重启后才从种子数据重新计算。 */
    private int nextId = 1000;

    /** 启动时加载并校验种子数据，失败直接让应用启动失败。 */
    @PostConstruct
    public void init() {
        reload();
    }

    /** 所有城市，按 id 升序。 */
    public List<CityDTO> list() {
        List<City> sorted = new ArrayList<>(cities.toList());
        sorted.sort(Comparator.comparingInt((City city)->city.getId()));
        List<CityDTO> result = new ArrayList<>(sorted.size());
        for (City city : sorted) {
            result.add(CityDTO.from(city));
        }
        return result;
    }

    /** 按 id 查询，不存在抛 404。 */
    public CityDTO get(int id) {
        return CityDTO.from(requireCity(id));
    }

    /**
     * 新增城市：名称唯一，id 由 {@link #nextCityId()} 分配（只增不减）。
     *
     * <p>按《接口与分层约定》3.1 的要求，插入位置跟随当前参照城市，
     * 这样链表自身保持“按距离有序”，不必等到 sorted 才排序。
     */
    public CityDTO add(CityRequest request) {
        String name = request.name().trim();
        requireNameAvailable(name, -1);
        requireCoordinate(request.longitude(), request.latitude());

        City city = new City(nextCityId(), name, request.longitude(), request.latitude());
        int reference = currentReferenceId();
        if (reference > 0) {
            cities.addByDistance(city, reference);
        } else {
            cities.addLast(city);
        }
        version++;
        return CityDTO.from(city);
    }

    /**
     * 修改名称或坐标。
     *
     * <p>坐标变化会影响排序，按 3.1 要求重排链表；名称变化不影响顺序。
     */
    public CityDTO update(int id, CityRequest request) {
        City city = requireCity(id);
        String name = request.name().trim();
        requireNameAvailable(name, id);
        requireCoordinate(request.longitude(), request.latitude());

        boolean coordinateChanged = city.getLongitude() != request.longitude()
            || city.getLatitude() != request.latitude();

        city.setName(name);
        city.setLongitude(request.longitude());
        city.setLatitude(request.latitude());

        if (coordinateChanged && cities.size() > 1) {
            cities.sortByDistance(currentReferenceId());
        }
        version++;
        return CityDTO.from(city);
    }

    /** 删除城市，不存在抛 404；删掉参照城市时回退到 id 最小的城市。 */
    public void delete(int id) {
        if (!cities.remove(id)) {
            throw new CityNotFoundException(id);
        }
        if (id == referenceId) {
            referenceId = smallestCityId();
        }
        version++;
    }

    /**
     * 按到参照城市的距离升序返回完整链表视图。
     *
     * <p>规则（《接口与分层约定》3.1）：
     * <ul>
     *   <li>referenceId 为空用 1000；</li>
     *   <li>参照城市被删则改用 id 最小的城市；</li>
     *   <li>返回 prevId/nextId，首尾按环回绕；</li>
     *   <li>没有城市时返回空列表，reference 为 null。</li>
     * </ul>
     */
    public SortedCitiesDTO sorted(Integer referenceId) {
        if (cities.isEmpty()) {
            return new SortedCitiesDTO(null, "asc", 0, List.of());
        }

        int requested = referenceId == null ? DEFAULT_REFERENCE_ID : referenceId;
        City reference = cities.find(requested);
        if (reference == null) {
            reference = cities.find(smallestCityId());
        }
        if (reference == null) {
            return new SortedCitiesDTO(null, "asc", 0, List.of());
        }

        int refId = reference.getId();
        this.referenceId = refId;
        cities.sortByDistance(refId);

        List<CityNode> nodes = cities.nodes();
        List<SortedCityDTO> items = new ArrayList<>(nodes.size());
        int firstId = nodes.get(0).getCity().getId();
        int lastId = nodes.get(nodes.size() - 1).getCity().getId();
        for (CityNode node : nodes) {
            City city = node.getCity();
            City prev = node.getPrev().getCity();
            City next = node.getNext().getCity();
            items.add(new SortedCityDTO(
                city.getId(),
                city.getName(),
                DistanceUtil.distance(reference, city),
                prev == null ? lastId : prev.getId(),
                next == null ? firstId : next.getId()));
        }

        return new SortedCitiesDTO(
            new CityRefDTO(reference.getId(), reference.getName()),
            "asc",
            firstId,
            items);
    }

    /** 恢复初始 30 城，供管理接口使用。 */
    public void reset() {
        reload();
        version++;
    }

    /** 当前数据版本，GraphService / MstService / PathService 的缓存依据。 */
    public int version() {
        return version;
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------

    private City requireCity(int id) {
        City city = cities.find(id);
        if (city == null) {
            throw new CityNotFoundException(id);
        }
        return city;
    }

    /**
     * 名称唯一校验。
     *
     * @param excludeId 修改时排除自己；新增传 -1
     */
    private void requireNameAvailable(String name, int excludeId) {
        for (City city : cities.toList()) {
            if (city.getId() != excludeId && city.getName().equals(name)) {
                throw new DuplicateCityException("城市名称已存在：" + name);
            }
        }
    }

    private void requireCoordinate(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException("经度必须在 -180 到 180 之间：" + longitude);
        }
        if (latitude < -90 || latitude > 90) {
            throw new InvalidCoordinateException("纬度必须在 -90 到 90 之间：" + latitude);
        }
    }

    // 优化②：从“每次扫描取最大值”改成“读计数器再自增”，O(n) → O(1)。
    // 优化③：加 synchronized，并发新增不会拿到同一个 id。
    /**
     * 取下一个城市 id 并自增。
     *
     * <p>只增不减：删掉最大 id 后再新增，不会复用旧 id。
     */
    private synchronized int nextCityId() {
        return nextId++;
    }

    /** 当前有效的参照城市 id；没有城市时返回 -1。 */
    private int currentReferenceId() {
        if (cities.find(referenceId) != null) {
            return referenceId;
        }
        return smallestCityId();
    }

    /** id 最小的城市 id；空表返回 -1。 */
    private int smallestCityId() {
        int min = Integer.MAX_VALUE;
        for (City city : cities.toList()) {
            min = Math.min(min, city.getId());
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }

    // 优化④：新增辅助方法，供 reload 计算“种子数据的最大 id”。
    /** 链表中最大的城市 id；空表返回 0。 */
    private int maxCityId(DoublyCircularLinkedList list) {
        int max = 0;
        for (City city : list.toList()) {
            max = Math.max(max, city.getId());
        }
        return max;
    }

    /** 从 classpath:/data/cities.txt 重新加载；校验失败抛 IllegalStateException。 */
    private void reload() {
        DoublyCircularLinkedList loaded = new DoublyCircularLinkedList();
        Set<String> names = new HashSet<>();

        try (InputStream input = getClass().getResourceAsStream("/data/cities.txt")) {
            if (input == null) {
                throw new IllegalStateException("找不到 classpath:/data/cities.txt");
            }
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                int lineNumber = 0;
                int index = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length != 3) {
                        throw new IllegalStateException(
                            "cities.txt 第 " + lineNumber + " 行格式错误，应为 name longitude latitude");
                    }
                    String name = parts[0];
                    double longitude;
                    double latitude;
                    try {
                        longitude = Double.parseDouble(parts[1]);
                        latitude = Double.parseDouble(parts[2]);
                    } catch (NumberFormatException ex) {
                        throw new IllegalStateException(
                            "cities.txt 第 " + lineNumber + " 行经纬度不是数字");
                    }
                    if (name.isEmpty() || name.length() > 20) {
                        throw new IllegalStateException(
                            "cities.txt 第 " + lineNumber + " 行城市名称长度不合法");
                    }
                    if (longitude < -180 || longitude > 180) {
                        throw new IllegalStateException(
                            "cities.txt 第 " + lineNumber + " 行经度越界：" + longitude);
                    }
                    if (latitude < -90 || latitude > 90) {
                        throw new IllegalStateException(
                            "cities.txt 第 " + lineNumber + " 行纬度越界：" + latitude);
                    }
                    if (!names.add(name)) {
                        throw new IllegalStateException(
                            "cities.txt 第 " + lineNumber + " 行城市名称重复：" + name);
                    }
                    loaded.addLast(new City(1000 + index, name, longitude, latitude));
                    index++;
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("读取 cities.txt 失败", ex);
        }

        if (loaded.size() != SEED_CITY_COUNT) {
            throw new IllegalStateException(
                "cities.txt 应包含 " + SEED_CITY_COUNT + " 条城市数据，实际 " + loaded.size() + " 条");
        }
        this.cities = loaded;
        this.referenceId = DEFAULT_REFERENCE_ID;
        // 优化④：取“当前计数器”和“种子最大 id + 1”的较大值。
        // 启动时是 1030；reset 后不会倒退，进程内 id 永远只增。
        this.nextId = Math.max(this.nextId, maxCityId(loaded) + 1);
    }
}

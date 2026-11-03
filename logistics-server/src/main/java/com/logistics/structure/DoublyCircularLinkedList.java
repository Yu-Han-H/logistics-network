package com.logistics.structure;

import com.logistics.model.City;
import com.logistics.util.DistanceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 双向循环链表，管理城市主数据。
 *
 * <p>带一个哨兵头节点 {@code head}，它不存城市：
 * <ul>
 *   <li>空表：{@code head.next == head.prev == head}；</li>
 *   <li>非空：{@code head.next} 是第一个城市，{@code head.prev} 是最后一个城市；</li>
 *   <li>从 {@code head.next} 一直往后走能回到 {@code head}，且每个节点的
 *       {@code next.prev == 自己}、{@code prev.next == 自己}。</li>
 * </ul>
 *
 * <p>本类只负责链表结构和按距离排序，不负责生成图、不负责版本号；
 * 增删改城市后的图重建由 Service 层统一编排（文档 4.7）。
 */
public class DoublyCircularLinkedList {
    private final CityNode head;
    private int size;

    public DoublyCircularLinkedList() {
        this.head = new CityNode(null);
        this.size = 0;
        this.head.prev = head;
        this.head.next = head;
    }

    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size==0;
    }

    public CityNode first() {
        return isEmpty()?null:head.next;
    }

    public CityNode last() {
        return isEmpty()?null:head.prev;
    }

    /**
     * 追加到尾部，O(1)。第一个城市、或按 id 顺序加载种子数据时用这个。
     *
     * @param city 城市，不能为 null，id 不能与已有城市重复
     * @return 新插入的节点
     * @throws IllegalArgumentException 城市为 null 或 id 重复时抛出
     */
    public CityNode addLast(City city) {
        requireAddable(city);
        CityNode node = new CityNode(city);
        linkBefore(head, node);
        size++;
        return node;
    }

    /**
     * 追加一个城市，语义与 {@link #addLast(City)} 相同。
     * 保留这个短名字，对应开发文档类图中的 {@code add(City)}。
     */
    public CityNode add(City city) {
        return addLast(city);
    }

    /**
     * 按到参照城市的距离升序插入，O(n)。新增城市时用这个，
     * 不用为了一次插入把整条链表重排。
     *
     * <p>注意：空链表没有参照城市，第一个城市请用 {@link #addLast(City)}。
     *
     * @param city        待插入城市，不能为 null，id 不能重复
     * @param referenceId 参照城市 id，必须在链表中
     * @return 新插入的节点
     * @throws IllegalArgumentException 城市为 null、id 重复或参照城市不存在时抛出
     */
    public CityNode addByDistance(City city, int referenceId) {
        if (city == null) {
            throw new IllegalArgumentException("城市不能为 null");
        }
        requireNoDuplicate(city.getId());

        City reference = find(referenceId);
        if (reference == null) {
            throw new IllegalArgumentException("参照城市不存在：" + referenceId);
        }
        double distance = DistanceUtil.distance(reference, city);

        CityNode position = head.next;
        while (position != head) {
            double other = DistanceUtil.distance(reference, position.city);
            // distance 已经取整到 2 位，可以直接精确比较；相等时按 id 升序
            if (distance < other || (distance == other && city.getId() < position.city.getId())) {
                break;
            }
            position = position.next;
        }

        CityNode node = new CityNode(city);
        linkBefore(position, node);
        size++;
        return node;
    }

    public City find(int cityId) {
        CityNode node = findNode(cityId);
        return node == null ? null : node.city;
    }

    public boolean contains(int cityId) {
        return findNode(cityId) != null;
    }

    /** 空表删除返回 false，不抛异常（文档 4.9）。 */
    public boolean remove(int cityId) {
        CityNode node = findNode(cityId);
        if (node == null) {
            return false;
        }
        unlink(node);
        size--;
        return true;
    }

    /**
     * 整体按到参照城市的距离升序重排，O(n²)：把节点一个个摘下来，
     * 按距离插到新环里（手写插入排序，禁止 Collections.sort）。
     *
     * @param referenceId 参照城市 id，必须在链表中
     * @throws IllegalArgumentException 参照城市不存在时抛出
     */
    public void sortByDistance(int referenceId) {
        if (size == 0) {
            return;
        }
        City reference = find(referenceId);
        if (reference == null) {
            throw new IllegalArgumentException("参照城市不存在：" + referenceId);
        }
        if (size == 1) {
            return;
        }

        // 先把距离算好，避免每次比较都算一遍 cos/sqrt
        Map<Integer, Double> distanceById = new HashMap<>(size);
        for (CityNode node = head.next; node != head; node = node.next) {
            distanceById.put(node.city.getId(), DistanceUtil.distance(reference, node.city));
        }

        // 摘空整个环，再逐个插回去
        CityNode cursor = head.next;
        head.next = head;
        head.prev = head;
        size = 0;

        while (cursor != head) {
            CityNode next = cursor.next;   // 必须先存，插入会改 cursor 的指针
            insertSorted(cursor, distanceById);
            cursor = next;
        }
    }

    /** 快照，给 Service 转 DTO 用；返回的是新 List，改它不影响链表。 */
    public List<City> toList() {
        List<City> cities = new ArrayList<>(size);
        for (CityNode node = head.next; node != head; node = node.next) {
            cities.add(node.city);
        }
        return cities;
    }

    /** 节点快照，给控制台打印 prev/next 用。 */
    public List<CityNode> nodes() {
        List<CityNode> nodes = new ArrayList<>(size);
        for (CityNode node = head.next; node != head; node = node.next) {
            nodes.add(node);
        }
        return Collections.unmodifiableList(nodes);
    }

    /** head → 北京 → 沈阳 → head，空表是 head → head。 */
    public String describe() {
        StringBuilder builder = new StringBuilder("head");
        for (CityNode node = head.next; node != head; node = node.next) {
            builder.append(" → ").append(node.city.getName());
        }
        return builder.append(" → head").toString();
    }

    @Override
    public String toString() {
        return describe();
    }

    /**
     * 校验四条不变量。测试里每个操作后调一次，答辩时也可以现场演示。
     */
    public void checkInvariants() {
        if (head.next == null || head.prev == null) {
            throw new IllegalStateException("哨兵指针为 null");
        }
        if (head.next.prev != head) {
            throw new IllegalStateException("head.next.prev != head");
        }
        if (head.prev.next != head) {
            throw new IllegalStateException("head.prev.next != head");
        }

        int counted = 0;
        Set<Integer> ids = new HashSet<>();
        for (CityNode node = head.next; node != head; node = node.next) {
            if (node == null) {
                throw new IllegalStateException("向前的链断了，遇到 null");
            }
            if (node.next == null || node.prev == null) {
                throw new IllegalStateException("节点指针为 null，id=" + node.city.getId());
            }
            if (node.next.prev != node) {
                throw new IllegalStateException("node.next.prev != node，id=" + node.city.getId());
            }
            if (node.prev.next != node) {
                throw new IllegalStateException("node.prev.next != node，id=" + node.city.getId());
            }
            if (!ids.add(node.city.getId())) {
                throw new IllegalStateException("城市 id 重复：" + node.city.getId());
            }
            counted++;
            if (counted > size) {
                throw new IllegalStateException("环没有回到 head，疑似死循环");
            }
        }
        if (counted != size) {
            throw new IllegalStateException("size 不一致：字段=" + size + "，实际=" + counted);
        }
    }

    private CityNode findNode(int cityId) {
        for (CityNode node = head.next; node != head; node = node.next) {
            if (node.city.getId() == cityId) {
                return node;
            }
        }
        return null;
    }

    /** 插入前的统一校验：非 null，且 id 不重复（文档 4.3 不变量 4）。 */
    private void requireAddable(City city) {
        if (city == null) {
            throw new IllegalArgumentException("城市不能为 null");
        }
        requireNoDuplicate(city.getId());
    }

    private void requireNoDuplicate(int cityId) {
        if (findNode(cityId) != null) {
            throw new IllegalArgumentException("城市 id 已存在：" + cityId);
        }
    }

    /** 把 node 接到 next 的前面。头插、尾插、中间插都走这一个方法。 */
    private void linkBefore(CityNode next, CityNode node) {
        CityNode prev = next.prev;
        node.prev = prev;
        node.next = next;
        prev.next = node;
        next.prev = node;
    }

    private void unlink(CityNode node) {
        CityNode prev = node.prev;
        CityNode next = node.next;
        prev.next = next;
        next.prev = prev;
        node.prev = null;
        node.next = null;
    }

    private void insertSorted(CityNode node, Map<Integer, Double> distanceById) {
        double distance = distanceOf(distanceById, node.city.getId());
        CityNode position = head.next;
        while (position != head) {
            double other = distanceOf(distanceById, position.city.getId());
            if (distance < other || (distance == other && node.city.getId() < position.city.getId())) {
                break;
            }
            position = position.next;
        }
        linkBefore(position, node);
        size++;
    }

    /**
     * 取出预先算好的距离。正常流程不会缺；显式判断可避免 Double 拆箱的
     * 「null type safety」提示，也方便定位排序过程中数据被并发修改的问题。
     */
    private double distanceOf(Map<Integer, Double> distanceById, int cityId) {
        Double distance = distanceById.get(cityId);
        if (distance == null) {
            throw new IllegalStateException("缺少城市距离：" + cityId);
        }
        return distance.doubleValue();
    }
}


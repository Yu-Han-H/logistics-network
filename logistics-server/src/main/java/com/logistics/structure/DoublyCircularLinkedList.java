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


public class DoublyCircularLinkedList {
    private final CityNode head;
    private int size;

    public DoublyCircularLinkedList() {
        this.head=new CityNode(null);
        this.size=0;
        this.head.prev=head;
        this.head.next=head;
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

    public CityNode addLast(City city) {
        if (city == null) {
            throw new IllegalArgumentException("城市不能为 null");
        }
        CityNode node = new CityNode(city);
        linkBefore(head, node);
        size++;
        return node;
    }



    public CityNode addByDistance(City city, int referenceId) {
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
     */
    public void sortByDistance(int referenceId) {
        if (size < 2) {
            return;
        }
        City reference = find(referenceId);
        if (reference == null) {
            throw new IllegalArgumentException("参照城市不存在：" + referenceId);
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
        double distance = distanceById.get(node.city.getId());
        CityNode position = head.next;
        while (position != head) {
            double other = distanceById.get(position.city.getId());
            if (distance < other || (distance == other && node.city.getId() < position.city.getId())) {
                break;
            }
            position = position.next;
        }
        linkBefore(position, node);
        size++;
    }
}


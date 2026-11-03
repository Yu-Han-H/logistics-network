package com.logistics.structure;

import com.logistics.model.City;
import com.logistics.util.DistanceUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 双向循环链表的单元测试，覆盖文档 9.2 的链表用例。
 */
class DoublyCircularLinkedListTest {

    @Test
    void emptyListIsCircularAndSafe() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();

        list.checkInvariants();
        assertThat(list.size()).isZero();
        assertThat(list.isEmpty()).isTrue();
        assertThat(list.first()).isNull();
        assertThat(list.last()).isNull();
        assertThat(list.find(1000)).isNull();
        assertThat(list.remove(1000)).isFalse();
        assertThat(list.describe()).isEqualTo("head → head");
    }

    @Test
    void addLastKeepsRingAndOrder() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(beijing());
        list.addLast(shanghai());
        list.addLast(shenyang());

        list.checkInvariants();
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.first().getCity().getName()).isEqualTo("北京");
        assertThat(list.last().getCity().getName()).isEqualTo("沈阳");
        assertThat(list.describe()).isEqualTo("head → 北京 → 上海 → 沈阳 → head");

        // 首节点的 prev 是哨兵 head，尾节点的 next 也是 head；
        // 首尾之间的前后指针互指，证明是双向循环
        List<CityNode> nodes = list.nodes();
        assertThat(nodes.get(0).getPrev().getCity()).isNull();   // 北京.prev == head
        assertThat(nodes.get(2).getNext().getCity()).isNull();   // 沈阳.next == head
        assertThat(nodes.get(0).getNext().getCity().getName()).isEqualTo("上海");
        assertThat(nodes.get(2).getPrev().getCity().getName()).isEqualTo("上海");
    }

    @Test
    void addByDistanceInsertsAscendingWithIdTieBreak() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(beijing());
        list.addByDistance(guangzhou(), 1000);
        list.addByDistance(shanghai(), 1000);
        list.addByDistance(shenyang(), 1000);

        list.checkInvariants();
        assertThat(list.toList())
            .extracting(City::getName)
            .containsExactly("北京", "沈阳", "上海", "广州");
    }

    @Test
    void sortByDistanceReordersInPlaceAndKeepsRing() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(guangzhou());
        list.addLast(shanghai());
        list.addLast(shenyang());
        list.addLast(beijing());

        list.sortByDistance(1000);
        list.checkInvariants();

        List<City> sorted = list.toList();
        assertThat(sorted)
            .extracting(City::getName)
            .containsExactly("北京", "沈阳", "上海", "广州");

        // 距离必须非递减
        City reference = list.find(1000);
        for (int i = 1; i < sorted.size(); i++) {
            double previous = DistanceUtil.distance(reference, sorted.get(i - 1));
            double current = DistanceUtil.distance(reference, sorted.get(i));
            assertThat(current).isGreaterThanOrEqualTo(previous);
        }
    }

    @Test
    void equalDistanceFallsBackToIdAscending() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        // 三城坐标完全一样，距离都为 0，只能按 id 升序
        list.addLast(new City(1009, "同点C", 116.41, 39.90));
        list.addLast(new City(1000, "同点A", 116.41, 39.90));
        list.addLast(new City(1008, "同点B", 116.41, 39.90));

        list.sortByDistance(1000);
        list.checkInvariants();

        assertThat(list.toList())
            .extracting(City::getId)
            .containsExactly(1000, 1008, 1009);
    }

    @Test
    void duplicateCityIdIsRejected() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(beijing());

        assertThatThrownBy(() -> list.addLast(beijing()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("已存在");
        assertThatThrownBy(() -> list.addByDistance(beijing(), 1000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("已存在");

        list.checkInvariants();
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    void removeHeadTailAndLastNodeKeepsRingValid() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(beijing());
        list.addLast(shanghai());
        list.addLast(shenyang());

        assertThat(list.remove(1000)).isTrue();  // 删除头节点
        list.checkInvariants();
        assertThat(list.first().getCity().getName()).isEqualTo("上海");

        assertThat(list.remove(1007)).isTrue();  // 删除尾节点
        list.checkInvariants();
        assertThat(list.last().getCity().getName()).isEqualTo("上海");

        assertThat(list.remove(1001)).isTrue();  // 删除最后一个节点
        list.checkInvariants();
        assertThat(list.isEmpty()).isTrue();
        assertThat(list.first()).isNull();
        assertThat(list.last()).isNull();
    }

    @Test
    void sortByDistanceRejectsUnknownReference() {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(beijing());

        assertThatThrownBy(() -> list.sortByDistance(9999))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("参照城市不存在");
    }

    private static City beijing() {
        return new City(1000, "北京", 116.41, 39.90);
    }

    private static City shanghai() {
        return new City(1001, "上海", 121.47, 31.23);
    }

    private static City guangzhou() {
        return new City(1002, "广州", 113.26, 23.13);
    }

    private static City shenyang() {
        return new City(1007, "沈阳", 123.43, 41.80);
    }
}

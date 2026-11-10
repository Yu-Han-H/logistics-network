package com.logistics.structure;

import com.logistics.model.City;

/**
 * 双向循环链表控制台自检（不依赖 JUnit）。
 *
 * <p>运行后打印 head → … → head 以及每个节点的 prev/next，
 * 最后校验链表四条不变量。对应文档 4.3 的链表演示输出。
 */
public class DoublyCircularLinkedListCheck {

    public static void main(String[] args) {
        DoublyCircularLinkedList list = new DoublyCircularLinkedList();
        list.addLast(new City(1000, "北京", 116.41, 39.90));
        list.addLast(new City(1001, "上海", 121.47, 31.23));
        list.addLast(new City(1007, "沈阳", 123.43, 41.80));

        System.out.println(list.describe());

        for (CityNode node : list.nodes()) {
            String prev = node.getPrev().getCity() == null
                ? "head" : node.getPrev().getCity().getName();
            String next = node.getNext().getCity() == null
                ? "head" : node.getNext().getCity().getName();
            System.out.printf("%-4s prev=%-4s next=%-4s%n",
                node.getCity().getName(), prev, next);
        }

        list.checkInvariants();
    }
}

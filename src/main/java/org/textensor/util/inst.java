package org.textensor.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class inst {
    public static <T> LinkedList<T> newLinkedList() {
        return new LinkedList<T>();
    }

    public static <T> LinkedList<T> newLinkedList(Collection<? extends T> c) {
        return new LinkedList<T>(c);
    }

    public static <T> LinkedList<T> newLinkedList(Iterator<? extends T> it) {
        LinkedList<T> list = new LinkedList<T>();
        while(it.hasNext())
            list.add(it.next());
        return list;
    }

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> newArrayList(int size) {
        return new ArrayList<T>(size);
    }

    public static <K,V> HashMap<K,V> newHashMap() {
        return new HashMap<K,V>();
    }

    public static <K,V> LinkedHashMap<K,V> newLinkedHashMap() {
        return new LinkedHashMap<K,V>();
    }

    public static <K,V> TreeMap<K,V> newTreeMap() {
        return new TreeMap<K,V>();
    }

    public static <T> TreeSet<T> newTreeSet() {
        return new TreeSet<T>();
    }

    public static <T> ArrayBlockingQueue<T> newArrayBlockingQueue(int capacity) {
        return new ArrayBlockingQueue<T>(capacity);
    }
}

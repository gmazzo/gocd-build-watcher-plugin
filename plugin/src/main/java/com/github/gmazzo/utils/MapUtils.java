package com.github.gmazzo.utils;

import java.util.HashMap;
import java.util.Map;

public final class MapUtils {

    public static <K, V> Map<K, V> map(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static <K, V> Map<K, V> map(K key1, V value1, K key2, V value2) {
        Map<K, V> map = map(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static <U> Map<U, U> map(U... pairs) {
        Map<U, U> map = new HashMap<>(pairs.length);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

}

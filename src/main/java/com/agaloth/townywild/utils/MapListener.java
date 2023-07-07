package com.agaloth.townywild.utils;

import java.util.HashMap;

public class MapListener<K, V> extends HashMap<K, V> {

    @Override
    public V remove(Object key) {
        new Exception().printStackTrace();
        return super.remove(key);
    }
}

package com.sucy.skill.util;

public class Pair<K, V> {

    private K first;

    private V last;

    public Pair(K first, V last) {
        this.first = first;
        this.last = last;
    }

    public K getKey() {
        return first;
    }

    public void setKey(K newKey) {
        this.first = newKey;
    }

    public V getLast() {
        return last;
    }

    public void setLast(V newLast) {
        this.last = newLast;
    }

}

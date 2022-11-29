package com.sucy.skill.util;

import java.util.concurrent.ConcurrentHashMap;

public class CooldownUtils {

    private static final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    public static boolean isCooldown(String key, Long tick) {
        Long now = System.currentTimeMillis();
        Long adder = tick * 20 * 1000;
        if (!map.containsKey(key)) {
            map.put(key, now + adder);
            return true;
        }
        Long getter = map.getOrDefault(key, 0L);
        if (getter <= now) {
            map.put(key, now + adder);
            return true;
        }
        return false;
    }

}

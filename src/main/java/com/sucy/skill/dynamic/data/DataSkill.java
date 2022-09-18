package com.sucy.skill.dynamic.data;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataSkill {

    private static ConcurrentHashMap<UUID, DataData> data = new ConcurrentHashMap<>();

    public static double getValue(UUID uuid, String key) {
        if (data.isEmpty()) {
            return 0;
        }
        if (!data.containsKey(uuid)) {
            return 0;
        }
        DataData gets = data.get(uuid);
        if (gets == null) {
            return 0;
        }
        DataTagData tagData = gets.getDataTagData(key);
        if (tagData == null) {
            return 0;
        }
        return tagData.getValue();
    }

    public static void setValue(UUID uuid, String key, Double value, Integer time) {
        if (data == null) {
            data = new ConcurrentHashMap<>();
        }
        if (!data.containsKey(uuid)) {
            data.put(uuid, new DataData(uuid));
        }
        DataData gets = data.get(uuid);
        gets.putDataTagData(key, value, time);
    }

    public static DataData getDataData(UUID uuid, boolean create) {
        if (data == null) {
            data = new ConcurrentHashMap<>();
        }
        if (!data.containsKey(uuid)) {
            if (create) {
                data.put(uuid, new DataData(uuid));
            } else {
                return null;
            }
        }
        return data.get(uuid);
    }

}

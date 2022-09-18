package com.sucy.skill.dynamic.data;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataData {

    public UUID uuid;

    private final ConcurrentHashMap<String, DataTagData> infos = new ConcurrentHashMap<>();

    public DataData(UUID uuid) {
        this.uuid = uuid;
    }

    public DataTagData getDataTagData(String key) {
        return getDataTagData(key, 0.0, 0, false);
    }

    public DataTagData getDataTagData(String key, Double vale, Integer tick, boolean create) {
        if (!infos.containsKey(key)) {
            if (create) {
                infos.put(key, new DataTagData(key, vale, tick));
            }
            return null;
        }
        return infos.get(key);
    }

    public void putDataTagData(String key, Double vale, Integer tick) {
        putDataTagData(key, vale, tick, "+");
    }

    public void putDataTagData(String key, Double vale, Integer tick, String action) {
        if (!infos.containsKey(key)) {
            infos.put(key, new DataTagData(key, vale, tick));
        }
        getDataTagData(key).setValue(vale, action);
    }
}

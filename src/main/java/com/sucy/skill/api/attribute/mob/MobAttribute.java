package com.sucy.skill.api.attribute.mob;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobAttribute {

    private static final ConcurrentHashMap<UUID, MobAttributeData> data = new ConcurrentHashMap<>();

    public static MobAttributeData getData(UUID uuid, boolean create) {
        if (data.containsKey(uuid)) {
            return data.get(uuid);
        }
        if (create) {
            return data.put(uuid, new MobAttributeData(uuid));
        }
        return null;
    }
}

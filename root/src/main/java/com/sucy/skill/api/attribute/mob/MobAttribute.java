package com.sucy.skill.api.attribute.mob;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobAttribute {

    public static ConcurrentHashMap<UUID, MobAttributeData> data = new ConcurrentHashMap<>();

    public static MobAttributeData getData(UUID uuid, boolean create) {
        if (create) {
            return data.computeIfAbsent(uuid, key -> new MobAttributeData(uuid));
        }
        return data.get(uuid);
    }

}

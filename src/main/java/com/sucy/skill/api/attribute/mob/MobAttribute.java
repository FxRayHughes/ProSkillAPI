package com.sucy.skill.api.attribute.mob;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobAttribute {

    public static ConcurrentHashMap<UUID, MobAttributeData> data = new ConcurrentHashMap<>();

    public static MobAttributeData getData(UUID uuid, boolean create) {
        if (data.containsKey(uuid)) {
            return data.get(uuid);
        }
        if (create) {
            MobAttributeData temp = new MobAttributeData(uuid);
            data.put(uuid, temp);
            return temp;
        }
        return null;
    }

}

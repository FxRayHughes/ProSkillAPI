package com.sucy.skill.task;

import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.thread.RepeatThreadTask;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;

public class MobAttributeTask extends RepeatThreadTask {

    public MobAttributeTask() {
        super(0, 20 * 30);
    }

    @Override
    public void run() {
        ArrayList<UUID> uuids = new ArrayList<>();
        MobAttribute.data.forEach((uuid, data) -> {
            if (Bukkit.getEntity(uuid) == null || Bukkit.getEntity(uuid).isDead()) {
                uuids.add(uuid);
            }
        });
        uuids.forEach(i -> MobAttribute.data.remove(i)
        );
    }
}

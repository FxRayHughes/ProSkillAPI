package com.sucy.skill.listener;

import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.data.PlayerEquipsUtils;
import com.sucy.skill.util.Pair;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;

import java.util.List;

public class MobListener extends SkillAPIListener {

    @EventHandler
    public static void onMobSpawn(MythicMobSpawnEvent event) {
        List<String> attribute = event.getMobType().getConfig().getStringList("psk-attribute");
        MobAttributeData data = MobAttribute.getData(event.getEntity().getUniqueId(), true);
        attribute.forEach(it -> {
            Pair<String, Integer> pair = PlayerEquipsUtils.getAttribute(it);
            if (data != null && pair != null && pair.getKey() != null) {
                data.addAttribute(pair.getKey(), pair.getLast());
            }
        });
    }
}

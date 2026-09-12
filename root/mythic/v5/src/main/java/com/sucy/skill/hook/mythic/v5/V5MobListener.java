package com.sucy.skill.hook.mythic.v5;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.MythicProvider;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * 怪物生成时把其配置里的 {@code psk-attribute} 写进 SkillAPI 的属性数据。
 */
public class V5MobListener implements Listener {

    private final MythicProvider provider;

    public V5MobListener(final MythicProvider provider) {
        this.provider = provider;
    }

    @EventHandler
    public void onMobSpawn(MythicMobSpawnEvent event) {
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) return;

        final List<String> attributes = provider.getMobAttributes(event.getEntity());
        for (String line : attributes) {
            bridge.applyMobAttribute(event.getEntity().getUniqueId(), line);
        }
    }
}

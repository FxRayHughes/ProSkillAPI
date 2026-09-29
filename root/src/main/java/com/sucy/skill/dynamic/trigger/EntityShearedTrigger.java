package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerShearEntityEvent;
import java.util.Map;

/** Fires after a player shears an entity; the event remains cancellable upstream. */
@SkillNode(key = "ENTITY_SHEARED", name = "Entity Sheared", nameZh = "实体被剪毛时",
        descriptionZh = "玩家成功剪取可剪毛实体时触发，施法者是玩家、目标是被剪实体；已取消的剪毛请求不会进入该阶段。", container = true)
public final class EntityShearedTrigger implements Trigger<PlayerShearEntityEvent> {
    @Override public String getKey() { return "ENTITY_SHEARED"; }
    @Override public Class<PlayerShearEntityEvent> getEvent() { return PlayerShearEntityEvent.class; }
    @Override public boolean shouldTrigger(PlayerShearEntityEvent event, int level, com.sucy.skill.api.Settings settings) { return true; }
    @Override public void setValues(PlayerShearEntityEvent event, Map<String, Object> data) {
        data.put("event-player", event.getPlayer());
        data.put("event-sheared-type", event.getEntity().getType().name());
    }
    @Override public LivingEntity getCaster(PlayerShearEntityEvent event) { return event.getPlayer(); }
    @Override public LivingEntity getTarget(PlayerShearEntityEvent event, com.sucy.skill.api.Settings settings) {
        return event.getEntity() instanceof LivingEntity ? (LivingEntity) event.getEntity() : event.getPlayer();
    }
}

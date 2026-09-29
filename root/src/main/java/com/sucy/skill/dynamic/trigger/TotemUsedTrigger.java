package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityResurrectEvent;
import java.util.Map;

/** Observes the resurrection phase after the entity consumes a totem. */
@SkillNode(key = "TOTEM_USED", name = "Totem Used", nameZh = "图腾生效时",
        descriptionZh = "实体被不死图腾救回时触发；这是复活事件阶段，施法者与初始目标都是获救实体。取消复活的事件不会进入执行。", container = true)
public final class TotemUsedTrigger implements Trigger<EntityResurrectEvent> {
    @Override public String getKey() { return "TOTEM_USED"; }
    @Override public Class<EntityResurrectEvent> getEvent() { return EntityResurrectEvent.class; }
    @Override public boolean shouldTrigger(EntityResurrectEvent event, int level, com.sucy.skill.api.Settings settings) { return true; }
    @Override public void setValues(EntityResurrectEvent event, Map<String, Object> data) {
        data.put("event-resurrected", event.getEntity());
    }
    @Override public LivingEntity getCaster(EntityResurrectEvent event) { return event.getEntity() instanceof LivingEntity ? (LivingEntity) event.getEntity() : null; }
    @Override public LivingEntity getTarget(EntityResurrectEvent event, com.sucy.skill.api.Settings settings) { return getCaster(event); }
}

package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import java.util.Map;

/** Shared adapter for acquired/lost target phases; one event yields two precise keys. */
public abstract class MobTargetTransitionTrigger implements Trigger<EntityTargetEvent> {
    protected abstract boolean acquired();
    @Override public Class<EntityTargetEvent> getEvent() { return EntityTargetEvent.class; }
    @Override public boolean shouldTrigger(EntityTargetEvent event, int level, com.sucy.skill.api.Settings settings) {
        return acquired() == (event.getTarget() != null);
    }
    @Override public void setValues(EntityTargetEvent event, Map<String, Object> data) {
        data.put("event-actor", event.getEntity());
        if (event.getTarget() != null) data.put("event-target", event.getTarget());
    }
    @Override public LivingEntity getCaster(EntityTargetEvent event) { return event.getEntity() instanceof LivingEntity ? (LivingEntity) event.getEntity() : null; }
    @Override public LivingEntity getTarget(EntityTargetEvent event, com.sucy.skill.api.Settings settings) {
        return event.getTarget() instanceof LivingEntity ? (LivingEntity) event.getTarget() : getCaster(event);
    }
}

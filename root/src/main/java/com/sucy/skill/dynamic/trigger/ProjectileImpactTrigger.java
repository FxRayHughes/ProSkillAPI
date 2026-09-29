package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Map;

/** Hit object access is reflective because 1.12 lacks newer Bukkit getters. */
public abstract class ProjectileImpactTrigger implements Trigger<ProjectileHitEvent> {
    @SkillField(kind = FieldKind.StringValue, label = "Projectile Type", labelZh = "投射物类型")
    private static final String TYPE = "projectile-type";
    @Override public Class<ProjectileHitEvent> getEvent() { return ProjectileHitEvent.class; }
    @Override public boolean shouldTrigger(ProjectileHitEvent event, int level, Settings settings) {
        String type = settings.getString(TYPE, "");
        return (type.isEmpty() || type.equalsIgnoreCase(event.getEntity().getType().name())) && matches(event);
    }
    protected abstract boolean matches(ProjectileHitEvent event);
    @Override public LivingEntity getCaster(ProjectileHitEvent event) {
        return event.getEntity().getShooter() instanceof LivingEntity
                ? (LivingEntity) event.getEntity().getShooter() : null;
    }
    @Override public LivingEntity getTarget(ProjectileHitEvent event, Settings settings) {
        Entity hit = hitEntity(event);
        return hit instanceof LivingEntity ? (LivingEntity) hit : getCaster(event);
    }
    @Override public void setValues(ProjectileHitEvent event, Map<String, Object> data) {
        data.put("event-actor", getCaster(event));
        data.put("event-projectile-type", event.getEntity().getType().name());
        Block block = hitBlock(event);
        data.put("event-impact-location", block == null
                ? event.getEntity().getLocation().clone() : block.getLocation().clone());
        Entity hit = hitEntity(event);
        if (hit != null) data.put("event-hit-type", hit.getType().name());
    }
    protected static Entity hitEntity(ProjectileHitEvent event) {
        Object value = getter(event, "getHitEntity");
        return value instanceof Entity ? (Entity) value : null;
    }
    protected static Block hitBlock(ProjectileHitEvent event) {
        Object value = getter(event, "getHitBlock");
        return value instanceof Block ? (Block) value : null;
    }
    private static Object getter(ProjectileHitEvent event, String method) {
        try { return event.getClass().getMethod(method).invoke(event); }
        catch (ReflectiveOperationException | LinkageError ex) { return null; }
    }
}

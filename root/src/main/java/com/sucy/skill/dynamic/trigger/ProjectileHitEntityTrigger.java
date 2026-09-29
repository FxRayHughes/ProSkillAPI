package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.entity.ProjectileHitEvent;

/** Entity impact is distinct from terrain impact even for the same projectile. */
@SkillNode(key = "PROJECTILE_HIT_ENTITY", name = "Projectile Hit Entity", nameZh = "投射物命中实体时",
        descriptionZh = "命中实体时触发；旧版 Bukkit 缺少命中对象 API 时该入口不触发。", container = true)
public final class ProjectileHitEntityTrigger extends ProjectileImpactTrigger {
    @Override public String getKey() { return "PROJECTILE_HIT_ENTITY"; }
    @Override protected boolean matches(ProjectileHitEvent event) { return hitEntity(event) != null; }
}

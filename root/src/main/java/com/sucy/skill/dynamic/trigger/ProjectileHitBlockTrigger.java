package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.entity.ProjectileHitEvent;

/** Does not infer a block hit from a missing entity hit on older Bukkit APIs. */
@SkillNode(key = "PROJECTILE_HIT_BLOCK", name = "Projectile Hit Block", nameZh = "投射物命中方块时",
        descriptionZh = "命中方块时触发；旧版 Bukkit 缺少命中方块 API 时该入口不触发。", container = true)
public final class ProjectileHitBlockTrigger extends ProjectileImpactTrigger {
    @Override public String getKey() { return "PROJECTILE_HIT_BLOCK"; }
    @Override protected boolean matches(ProjectileHitEvent event) { return hitBlock(event) != null; }
}

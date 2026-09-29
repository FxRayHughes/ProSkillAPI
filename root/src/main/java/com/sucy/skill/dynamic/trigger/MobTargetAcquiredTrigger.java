package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

@SkillNode(key = "MOB_TARGET_ACQUIRED", name = "Mob Target Acquired", nameZh = "生物获得目标时",
        descriptionZh = "生物获得一个有效仇恨目标时触发；初始目标为新目标，施法者是产生变化的生物。", container = true)
public final class MobTargetAcquiredTrigger extends MobTargetTransitionTrigger {
    @Override public String getKey() { return "MOB_TARGET_ACQUIRED"; }
    @Override protected boolean acquired() { return true; }
}

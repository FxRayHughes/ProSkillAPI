package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

@SkillNode(key = "MOB_TARGET_LOST", name = "Mob Target Lost", nameZh = "生物失去目标时",
        descriptionZh = "生物目标被清空时触发；由于 Bukkit 事件没有旧目标对象，初始目标回退为发生变化的生物。", container = true)
public final class MobTargetLostTrigger extends MobTargetTransitionTrigger {
    @Override public String getKey() { return "MOB_TARGET_LOST"; }
    @Override protected boolean acquired() { return false; }
}

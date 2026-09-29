package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Committed experience phase with the same source and amount fields as other outcomes. */
@SkillNode(key = "VANILLA_XP_CHANGED", name = "VanillaXpChanged", nameZh = "原版经验改变后",
        descriptionZh = "事件在数值生效后触发，提供变化量。", container = true)
public final class VanillaXpChangedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "VANILLA_XP_CHANGED"; }
}

package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Committed experience phase with the same source and amount fields as other outcomes. */
@SkillNode(key = "CLASS_XP_LOST", name = "ClassXpLost", nameZh = "职业经验减少后",
        descriptionZh = "事件在数值生效后触发，提供变化量。", container = true)
public final class ClassXpLostTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "CLASS_XP_LOST"; }
}

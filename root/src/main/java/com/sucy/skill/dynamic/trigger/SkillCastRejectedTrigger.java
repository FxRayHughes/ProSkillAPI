package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "SKILL_CAST_REJECTED", name = "SkillCastRejected", nameZh = "技能施放被拒绝时",
        descriptionZh = "提供拒绝原因；不代表技能执行。", container = true)
public final class SkillCastRejectedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "SKILL_CAST_REJECTED"; }
}

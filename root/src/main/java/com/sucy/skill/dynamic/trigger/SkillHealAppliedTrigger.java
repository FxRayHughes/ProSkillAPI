package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "SKILL_HEAL_APPLIED", name = "SkillHealApplied", nameZh = "技能治疗生效后",
        descriptionZh = "提供实际生命变化量。", container = true)
public final class SkillHealAppliedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "SKILL_HEAL_APPLIED"; }
}

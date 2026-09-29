package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "SKILL_LEVEL_REDUCED", name = "SkillLevelReduced", nameZh = "技能等级降低后",
        descriptionZh = "包括强制降级，提供变更前后等级。", container = true)
public final class SkillLevelReducedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "SKILL_LEVEL_REDUCED"; }
}

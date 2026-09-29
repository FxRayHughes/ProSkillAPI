package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "SKILL_LEVEL_RAISED", name = "SkillLevelRaised", nameZh = "技能等级增加后",
        descriptionZh = "包括强制升级，提供变更前后等级。", container = true)
public final class SkillLevelRaisedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "SKILL_LEVEL_RAISED"; }
}

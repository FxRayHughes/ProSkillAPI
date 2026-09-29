package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "SKILL_CAST_ACCEPTED", name = "SkillCastAccepted", nameZh = "技能施放成功后",
        descriptionZh = "消耗和冷却已经提交。", container = true)
public final class SkillCastAcceptedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "SKILL_CAST_ACCEPTED"; }
}

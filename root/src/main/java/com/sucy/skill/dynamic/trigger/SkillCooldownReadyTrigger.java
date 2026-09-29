package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "SKILL_COOLDOWN_READY", name = "SkillCooldownReady", nameZh = "技能冷却结束时",
        descriptionZh = "从冷却状态转为可用时触发。", container = true)
public final class SkillCooldownReadyTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "SKILL_COOLDOWN_READY"; }
}

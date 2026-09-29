package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "FLAG_STARTED", name = "FlagStarted", nameZh = "标记生效后",
        descriptionZh = "提供标记名称和期限。", container = true)
public final class FlagStartedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "FLAG_STARTED"; }
}

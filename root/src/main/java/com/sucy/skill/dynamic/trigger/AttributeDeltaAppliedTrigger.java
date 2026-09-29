package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Distinct committed phase sharing OutcomeTrigger's actor and payload mapping. */
@SkillNode(key = "ATTRIBUTE_DELTA_APPLIED", name = "AttributeDeltaApplied", nameZh = "属性变化生效后",
        descriptionZh = "提供属性键与本次变化量。", container = true)
public final class AttributeDeltaAppliedTrigger extends OutcomeTrigger {
    @Override public String getKey() { return "ATTRIBUTE_DELTA_APPLIED"; }
}

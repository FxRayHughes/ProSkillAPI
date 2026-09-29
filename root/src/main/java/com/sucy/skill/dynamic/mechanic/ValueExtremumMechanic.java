package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** One node selects a minimum or maximum without duplicate source fields. */
@SkillNode(key = "value extremum", name = "Value Extremum", nameZh = "数值极值", descriptionZh = "根据模式选出 A 与 B 的最小值或最大值。")
public final class ValueExtremumMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value extremum"; }
}

package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** B and C are ordered bounds; an inverted interval is rejected. */
@SkillNode(key = "value clamp", name = "Value Clamp", nameZh = "数值钳制", descriptionZh = "把 A 限制在 B 与 C 的闭区间。")
public final class ValueClampMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value clamp"; }
}

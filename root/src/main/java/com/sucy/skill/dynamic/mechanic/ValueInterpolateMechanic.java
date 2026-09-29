package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** C is a normalized interpolation factor, so extrapolation is rejected. */
@SkillNode(key = "value interpolate", name = "Value Interpolate", nameZh = "数值插值", descriptionZh = "按 C 在 A 与 B 之间线性插值，C 必须在零到一之间。")
public final class ValueInterpolateMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value interpolate"; }
}

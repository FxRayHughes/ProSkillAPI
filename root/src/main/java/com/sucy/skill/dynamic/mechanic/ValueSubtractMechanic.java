package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Subtraction uses the shared typed source model and stores a finite result. */
@SkillNode(key = "value subtract", name = "Value Subtract", nameZh = "数值相减", descriptionZh = "计算 A 减 B 并写入施法数据。")
public final class ValueSubtractMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value subtract"; }
}

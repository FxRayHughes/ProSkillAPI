package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Rejects a zero divisor before touching cast data. */
@SkillNode(key = "value divide", name = "Value Divide", nameZh = "数值相除", descriptionZh = "计算 A 除以 B，B 为零时不执行。")
public final class ValueDivideMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value divide"; }
}

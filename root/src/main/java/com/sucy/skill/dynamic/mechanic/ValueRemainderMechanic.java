package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Remainder shares divide's zero and finite-value safeguards. */
@SkillNode(key = "value remainder", name = "Value Remainder", nameZh = "数值取余", descriptionZh = "计算 A 对 B 的余数，B 为零时不执行。")
public final class ValueRemainderMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value remainder"; }
}

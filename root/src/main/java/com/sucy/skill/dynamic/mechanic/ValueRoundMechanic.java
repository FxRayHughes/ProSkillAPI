package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Precision is an integer in [0,9] to avoid overflow from arbitrary powers of ten. */
@SkillNode(key = "value round", name = "Value Round", nameZh = "数值取整", descriptionZh = "按 B 指定的小数位数取整；支持四舍五入、向上与向下。")
public final class ValueRoundMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value round"; }
}

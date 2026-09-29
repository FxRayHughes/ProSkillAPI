package com.sucy.skill.dynamic.mechanic;
import com.sucy.skill.dynamic.meta.SkillNode;
/** The signed shortest rotation is normalized to [-180,180). */
@SkillNode(key = "value angle", name = "Value Angle", nameZh = "最短角度差", descriptionZh = "计算从角度 A 到 B 的有符号最短旋转。")
public final class ValueAngleMechanic extends AbstractValueOperation {
    @Override public String getKey() { return "value angle"; }
}

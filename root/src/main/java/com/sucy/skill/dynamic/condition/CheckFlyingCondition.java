package com.sucy.skill.dynamic.condition;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Flight permission and current flight are separate; this reads current flight. */
@SkillNode(key = "check flying", name = "Check Flying", nameZh = "检查飞行", descriptionZh = "检查目标玩家是否正在飞行，而非是否有飞行许可。", container = true)
public final class CheckFlyingCondition extends AbstractPlayerStateCondition { @Override public String getKey() { return "check flying"; } }

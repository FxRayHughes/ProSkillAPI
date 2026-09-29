package com.sucy.skill.dynamic.condition;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Checks an active player's confirmed sprint state. */
@SkillNode(key = "check sprint", name = "Check Sprint", nameZh = "检查疾跑", descriptionZh = "检查目标玩家当前是否疾跑。", container = true)
public final class CheckSprintCondition extends AbstractPlayerStateCondition { @Override public String getKey() { return "check sprint"; } }

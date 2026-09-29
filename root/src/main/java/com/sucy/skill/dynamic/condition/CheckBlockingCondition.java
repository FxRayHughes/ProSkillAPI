package com.sucy.skill.dynamic.condition;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Reads Bukkit's live shield-blocking state on a player. */
@SkillNode(key = "check blocking", name = "Check Blocking", nameZh = "检查格挡", descriptionZh = "检查目标玩家是否正在原版格挡。", container = true)
public final class CheckBlockingCondition extends AbstractPlayerStateCondition { @Override public String getKey() { return "check blocking"; } }

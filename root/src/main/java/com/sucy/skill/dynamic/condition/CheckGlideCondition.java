package com.sucy.skill.dynamic.condition;
import com.sucy.skill.dynamic.meta.SkillNode;
/** Checks actual glide state rather than the last toggle request. */
@SkillNode(key = "check glide", name = "Check Glide", nameZh = "检查滑翔", descriptionZh = "检查目标玩家当前是否滑翔。", container = true)
public final class CheckGlideCondition extends AbstractPlayerStateCondition { @Override public String getKey() { return "check glide"; } }

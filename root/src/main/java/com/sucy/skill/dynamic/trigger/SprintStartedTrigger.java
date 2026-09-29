package com.sucy.skill.dynamic.trigger;
import com.sucy.skill.dynamic.meta.SkillNode;
/**
 * Emits after a sprint request has been accepted and the player is confirmed
 * to be sprinting on the following tick. A cancelled request produces no
 * event, so child mechanics can safely treat this as a completed transition.
 */
@SkillNode(key = "SPRINT_STARTED", name = "Sprint Started", nameZh = "开始疾跑后",
        descriptionZh = "玩家的疾跑请求通过其他监听器处理后，下一 tick 确认玩家仍处于疾跑状态时触发；仅表示状态已经成立，不表示请求事件本身未被取消。", container = true)
public final class SprintStartedTrigger extends StateTrigger { @Override public String getKey() { return "SPRINT_STARTED"; } }

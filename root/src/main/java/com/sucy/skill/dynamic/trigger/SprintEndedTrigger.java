package com.sucy.skill.dynamic.trigger;
import com.sucy.skill.dynamic.meta.SkillNode;
/**
 * Emits after a sprint stop request has been accepted and the player is
 * confirmed to have left sprinting state on the following tick. It describes
 * the completed state transition rather than the cancellable request event.
 */
@SkillNode(key = "SPRINT_ENDED", name = "Sprint Ended", nameZh = "结束疾跑后",
        descriptionZh = "玩家结束疾跑的请求通过其他监听器处理后，下一 tick 确认玩家已经不再疾跑时触发；后续子节点读取的是确认后的玩家状态。", container = true)
public final class SprintEndedTrigger extends StateTrigger { @Override public String getKey() { return "SPRINT_ENDED"; } }

package com.sucy.skill.dynamic.trigger;
import com.sucy.skill.dynamic.meta.SkillNode;
/**
 * Emits after a glide stop request has been accepted and the entity is
 * confirmed to have left gliding state on the following tick. A cancelled
 * request is intentionally invisible to this completed-transition trigger.
 */
@SkillNode(key = "GLIDE_ENDED", name = "Glide Ended", nameZh = "结束滑翔后",
        descriptionZh = "滑翔结束请求处理完成，并在下一 tick 确认实体已经离开滑翔状态时触发；子节点不会把被取消的结束请求误判为已完成。", container = true)
public final class GlideEndedTrigger extends StateTrigger { @Override public String getKey() { return "GLIDE_ENDED"; } }

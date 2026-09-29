package com.sucy.skill.dynamic.trigger;
import com.sucy.skill.dynamic.meta.SkillNode;
/**
 * Emits after a glide request has been accepted and the entity is confirmed
 * to be gliding on the following tick. The event is post-confirmation and
 * cannot be used to cancel the original toggle request.
 */
@SkillNode(key = "GLIDE_STARTED", name = "Glide Started", nameZh = "开始滑翔后",
        descriptionZh = "滑翔切换请求未被取消，并在下一 tick 确认实体已进入滑翔状态时触发；适用于开始滑翔后的效果，不用于拦截原始请求。", container = true)
public final class GlideStartedTrigger extends StateTrigger { @Override public String getKey() { return "GLIDE_STARTED"; } }

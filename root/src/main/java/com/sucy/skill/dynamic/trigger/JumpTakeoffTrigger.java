package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;

/** Emits once when a player leaves the ground with an upward movement sample. */
@SkillNode(key = "JUMP_TAKEOFF", name = "Jump Takeoff", nameZh = "跳跃起跳时",
        descriptionZh = "玩家从着地状态进入上升状态时触发一次；只使用移动事件的状态边界，不把空中持续移动重复算成跳跃。", container = true)
public final class JumpTakeoffTrigger extends StateTrigger {
    @Override public String getKey() { return "JUMP_TAKEOFF"; }
}

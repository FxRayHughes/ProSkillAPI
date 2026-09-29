package com.sucy.skill.dynamic.trigger;
import com.sucy.skill.dynamic.meta.SkillNode;
/**
 * Emits after a flight request has been accepted and the player is confirmed
 * to be flying on the following tick. Having flight permission alone does not
 * produce this trigger because permission is not the same as active flight.
 */
@SkillNode(key = "FLIGHT_STARTED", name = "Flight Started", nameZh = "开始飞行后",
        descriptionZh = "飞行切换请求完成后，下一 tick 确认玩家实际进入飞行状态时触发；拥有飞行许可但尚未开始飞行不会触发。", container = true)
public final class FlightStartedTrigger extends StateTrigger { @Override public String getKey() { return "FLIGHT_STARTED"; } }

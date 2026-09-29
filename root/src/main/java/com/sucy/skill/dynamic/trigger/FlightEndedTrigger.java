package com.sucy.skill.dynamic.trigger;
import com.sucy.skill.dynamic.meta.SkillNode;
/**
 * Emits after a flight stop request has been accepted and the player is
 * confirmed not to be flying on the following tick. Changing a permission
 * without changing active flight does not produce this completed transition.
 */
@SkillNode(key = "FLIGHT_ENDED", name = "Flight Ended", nameZh = "结束飞行后",
        descriptionZh = "飞行结束请求处理完成后，下一 tick 确认玩家实际不再飞行时触发；仅改变飞行许可而没有结束当前飞行不会产生此事件。", container = true)
public final class FlightEndedTrigger extends StateTrigger { @Override public String getKey() { return "FLIGHT_ENDED"; } }

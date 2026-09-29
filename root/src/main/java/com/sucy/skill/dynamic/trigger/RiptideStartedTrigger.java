package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.player.PlayerRiptideEvent;
import java.util.Map;

/** Fires when a trident's Riptide launch has been accepted by Bukkit. */
@SkillNode(key = "RIPTIDE_STARTED", name = "Riptide Started", nameZh = "激流启动时",
        descriptionZh = "玩家使用激流三叉戟开始移动时触发；施法者与目标都是该玩家。事件若被其他插件取消则不执行。", container = true)
public final class RiptideStartedTrigger extends PlayerEventTrigger<PlayerRiptideEvent> {
    @Override public String getKey() { return "RIPTIDE_STARTED"; }
    @Override public Class<PlayerRiptideEvent> getEvent() { return PlayerRiptideEvent.class; }
    @Override public void setValues(PlayerRiptideEvent event, Map<String, Object> data) {
        data.put("event-player", event.getPlayer());
    }
}

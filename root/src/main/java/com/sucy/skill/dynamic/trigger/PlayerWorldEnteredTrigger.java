package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Map;

/** PlayerChangedWorldEvent runs after the destination world becomes current. */
@SkillNode(key = "PLAYER_WORLD_ENTERED", name = "Player World Entered", nameZh = "进入世界后",
        descriptionZh = "玩家跨世界完成后触发，写入原世界与目标世界名。", container = true)
public final class PlayerWorldEnteredTrigger extends PlayerEventTrigger<PlayerChangedWorldEvent> {
    @Override public String getKey() { return "PLAYER_WORLD_ENTERED"; }
    @Override public Class<PlayerChangedWorldEvent> getEvent() { return PlayerChangedWorldEvent.class; }
    @Override public void setValues(PlayerChangedWorldEvent event, Map<String, Object> data) {
        super.setValues(event, data);
        data.put("event-world-from", event.getFrom().getName());
        data.put("event-world-to", event.getPlayer().getWorld().getName());
    }
}

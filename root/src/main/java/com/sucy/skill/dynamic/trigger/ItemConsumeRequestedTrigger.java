package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.Map;

/** Fires on the cancellable request, before food or potion effects apply. */
@SkillNode(key = "ITEM_CONSUME_REQUESTED", name = "Item Consume Requested", nameZh = "请求消耗物品时",
        descriptionZh = "可取消的原版消耗请求；不代表物品已经消耗。", container = true)
public final class ItemConsumeRequestedTrigger extends PlayerEventTrigger<PlayerItemConsumeEvent> {
    @Override public String getKey() { return "ITEM_CONSUME_REQUESTED"; }
    @Override public Class<PlayerItemConsumeEvent> getEvent() { return PlayerItemConsumeEvent.class; }
    @Override public void setValues(PlayerItemConsumeEvent event, Map<String, Object> data) {
        super.setValues(event, data);
        data.put("event-item-type", event.getItem().getType().name());
    }
}

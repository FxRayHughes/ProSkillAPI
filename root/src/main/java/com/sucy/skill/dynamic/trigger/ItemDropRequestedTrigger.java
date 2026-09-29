package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Map;

/** Fires on the cancellable drop request, while the item entity still exists. */
@SkillNode(key = "ITEM_DROP_REQUESTED", name = "Item Drop Requested", nameZh = "请求丢弃物品时",
        descriptionZh = "可取消的原版丢弃请求；不代表掉落已被接受。", container = true)
public final class ItemDropRequestedTrigger extends PlayerEventTrigger<PlayerDropItemEvent> {
    @Override public String getKey() { return "ITEM_DROP_REQUESTED"; }
    @Override public Class<PlayerDropItemEvent> getEvent() { return PlayerDropItemEvent.class; }
    @Override public void setValues(PlayerDropItemEvent event, Map<String, Object> data) {
        super.setValues(event, data);
        data.put("event-item-type", event.getItemDrop().getItemStack().getType().name());
        data.put("event-item-amount", event.getItemDrop().getItemStack().getAmount());
    }
}

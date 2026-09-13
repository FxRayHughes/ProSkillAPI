package com.sucy.skill.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import java.util.Collections;
import java.util.Map;

/** 套装刷新后的完整属性快照，供外部属性插件替换同一来源而避免重复叠加。 */
public final class SetBonusAttributeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Map<String, Integer> bonuses;
    public SetBonusAttributeEvent(Player player, Map<String, Integer> bonuses) {
        this.player = player;
        this.bonuses = Collections.unmodifiableMap(new java.util.HashMap<>(bonuses));
    }
    public Player getPlayer() { return player; }
    public Map<String, Integer> getBonuses() { return bonuses; }
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerEvent;

import java.util.Map;

/**
 * Shared adapter for player-originated Bukkit events.
 *
 * <p>These events already identify the player that caused the action, so the
 * adapter deliberately uses that same player as caster and target. Concrete
 * subclasses still define the semantic phase in their node metadata; this
 * class only supplies the common routing and the {@code event-player} value.</p>
 */
public abstract class PlayerEventTrigger<E extends PlayerEvent> implements Trigger<E> {
    /** Player request filters are defined by each concrete trigger's fields. */
    @Override public boolean shouldTrigger(E event, int level, Settings settings) { return true; }
    /** @return player that emitted the Bukkit event */
    @Override public LivingEntity getCaster(E event) { return event.getPlayer(); }
    /** @return same player, because player events have no separate target */
    @Override public LivingEntity getTarget(E event, Settings settings) { return event.getPlayer(); }
    /** Adds the player to the shared event-data namespace for child mechanics. */
    @Override public void setValues(E event, Map<String, Object> data) {
        data.put("event-player", event.getPlayer());
    }
}

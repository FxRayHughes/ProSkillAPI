package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/**
 * Shared adapter for confirmed movement-state transitions and airborne samples.
 *
 * <p>Started and ended nodes compare their key with the confirmed event kind.
 * The air-threshold node compares equality with one configured sample count;
 * this gives each skill one firing at its threshold without allocating a
 * per-skill counter in the movement listener. Values below one are normalized
 * to one because an airborne interval cannot have a meaningful zero threshold.</p>
 */
public abstract class StateTrigger implements Trigger<StateTransitionEvent> {
    protected static final String AIR_THRESHOLD = "air-threshold";
    /** All state nodes consume the same confirmed transition event. */
    @Override public Class<StateTransitionEvent> getEvent() { return StateTransitionEvent.class; }
    @Override public boolean shouldTrigger(StateTransitionEvent event, int level, Settings settings) {
        if (!event.getKind().name().equals(getKey())) return false;
        if (event.getKind() == StateTransitionEvent.Kind.AIR_THRESHOLD_CROSSED)
            return event.getAirTicks() == Math.max(1, settings.getInt(AIR_THRESHOLD, 10));
        return true;
    }
    @Override public LivingEntity getCaster(StateTransitionEvent event) { return event.getEntity(); }
    @Override public LivingEntity getTarget(StateTransitionEvent event, Settings settings) { return event.getEntity(); }
    @Override public void setValues(StateTransitionEvent event, Map<String, Object> data) {
        data.put("event-state", event.getKind().name());
    }
}

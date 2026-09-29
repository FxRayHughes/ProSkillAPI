package com.sucy.skill.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Published after Bukkit confirms a movement state change on the next tick.
 *
 * <p>Sprint, glide, and flight Bukkit events describe a requested toggle and
 * may still be cancelled by another listener. The listener therefore checks
 * the entity's actual state on the following tick before publishing the
 * corresponding started or ended event. Jump and air-threshold events are
 * samples from player movement; they are not promises that a player has a
 * particular velocity or that a jump animation was sent to the client.</p>
 */
public final class StateTransitionEvent extends Event {
    /** State transitions and movement samples exposed to dynamic triggers. */
    public enum Kind {
        SPRINT_STARTED, SPRINT_ENDED,
        GLIDE_STARTED, GLIDE_ENDED,
        FLIGHT_STARTED, FLIGHT_ENDED,
        JUMP_TAKEOFF, AIR_THRESHOLD_CROSSED
    }
    private static final HandlerList HANDLERS = new HandlerList();
    private final Kind kind;
    private final LivingEntity entity;
    private final int airTicks;
    /** Creates a state event without an airborne sample count. */
    public StateTransitionEvent(Kind kind, LivingEntity entity) { this(kind, entity, 0); }
    /**
     * Creates a state event with the number of movement samples spent airborne.
     *
     * @param kind transition or sample type
     * @param entity player or living entity whose state was observed
     * @param airTicks number of off-ground samples; zero for non-air events
     */
    public StateTransitionEvent(Kind kind, LivingEntity entity, int airTicks) {
        this.kind = kind;
        this.entity = entity;
        this.airTicks = airTicks;
    }
    /** @return transition or sample type used by trigger matching */
    public Kind getKind() { return kind; }
    /** @return entity whose confirmed state produced this event */
    public LivingEntity getEntity() { return entity; }
    /**
     * Returns the number of movement samples spent off the ground when the
     * threshold was crossed. The counter is reset after the entity lands.
     *
     * @return positive airborne sample count for threshold events, otherwise zero
     */
    public int getAirTicks() { return airTicks; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

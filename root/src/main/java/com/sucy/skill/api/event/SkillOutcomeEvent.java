package com.sucy.skill.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Reports a committed skill outcome after its source operation has finished.
 *
 * <p>This event is deliberately limited to observations that are already
 * committed. A listener can use it to start a follow-up skill or update a
 * display, but changing a value in this event cannot roll back the operation
 * that produced it. Requested or cancellable phases keep their existing public
 * event types so callers can still cancel a cast, item action, or damage event
 * at the point where the server accepts that request.</p>
 *
 * <p>{@code previous}, {@code current}, and {@code amount} are populated by the
 * producer according to the phase. Consumers must treat values that do not
 * apply to a phase as informational defaults rather than infer a second event
 * contract from them.</p>
 */
public final class SkillOutcomeEvent extends Event {
    /** Stable phase names used by dynamic trigger keys and external listeners. */
    public enum Phase {
        CAST_ACCEPTED, CAST_REJECTED, LEVEL_RAISED, LEVEL_REDUCED,
        HEAL_APPLIED, FLAG_STARTED, ATTRIBUTE_DELTA_APPLIED, COOLDOWN_READY,
        CLASS_XP_GAINED, CLASS_XP_LOST, VANILLA_XP_CHANGED
    }

    private static final HandlerList HANDLERS = new HandlerList();
    private final Phase phase;
    private final LivingEntity actor;
    private final LivingEntity target;
    private final String name;
    private final String source;
    private final double amount;
    private final double previous;
    private final double current;

    /**
     * Creates an immutable post-commit notification.
     *
     * @param phase operation phase represented by this event; never {@code null}
     * @param actor entity that caused or owns the operation
     * @param target affected entity when the phase has one, otherwise {@code null}
     * @param name skill, flag, attribute, or other domain name used for filtering
     * @param source human-readable source or subsystem label
     * @param amount signed change or event amount, as defined by the phase
     * @param previous value before the operation when a before value exists
     * @param current value after the operation when an after value exists
     */
    public SkillOutcomeEvent(Phase phase, LivingEntity actor, LivingEntity target, String name,
                             String source, double amount, double previous, double current) {
        this.phase = phase;
        this.actor = actor;
        this.target = target;
        this.name = name;
        this.source = source;
        this.amount = amount;
        this.previous = previous;
        this.current = current;
    }

    /** @return committed operation phase used by trigger matching */
    public Phase getPhase() { return phase; }
    /** @return entity that owns or caused the operation */
    public LivingEntity getActor() { return actor; }
    /** @return affected entity, or {@code null} for actor-only phases */
    public LivingEntity getTarget() { return target; }
    /** @return domain name used by the optional trigger source filter */
    public String getName() { return name; }
    /** @return subsystem or source label supplied by the producer */
    public String getSource() { return source; }
    /** @return phase-specific amount or signed delta */
    public double getAmount() { return amount; }
    /** @return value observed before the committed change */
    public double getPrevious() { return previous; }
    /** @return value observed after the committed change */
    public double getCurrent() { return current; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

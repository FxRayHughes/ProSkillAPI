package com.sucy.skill.combat.shield;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Reports a completed change to one shield layer.
 *
 * <p>The event is fired after the layer list has been updated. {@code amount}
 * is the capacity added, absorbed, depleted, expired, or removed by this one
 * layer, while {@code remaining} is the layer's live capacity at notification
 * time. Events are observational: listeners must not assume that cancelling or
 * mutating the event can undo a hit or restore a removed layer. Use the shield
 * mechanics or {@link ShieldManager} methods for a new mutation.</p>
 */
public final class ShieldEvent extends Event {
    /** Stable lifecycle stages used by shield triggers and integrations. */
    public enum Kind { APPLIED, ABSORBED, DEPLETED, EXPIRED, REMOVED }

    private static final HandlerList HANDLERS = new HandlerList();
    private final Kind kind;
    private final LivingEntity target;
    private final UUID sourceId;
    private final UUID layerId;
    private final String key;
    private final double amount;
    private final double remaining;

    /**
     * Creates a notification for a single layer transition.
     *
     * @param kind lifecycle transition that has already happened
     * @param target entity owning the layer
     * @param sourceId entity that granted the layer, or {@code null} for an external grant
     * @param layerId stable identity of the affected layer
     * @param key user-facing stacking and lookup key
     * @param amount capacity involved in this transition
     * @param remaining capacity left after the transition
     */
    public ShieldEvent(Kind kind, LivingEntity target, UUID sourceId, UUID layerId,
                       String key, double amount, double remaining) {
        this.kind = kind;
        this.target = target;
        this.sourceId = sourceId;
        this.layerId = layerId;
        this.key = key;
        this.amount = amount;
        this.remaining = remaining;
    }

    /** @return completed lifecycle transition */
    public Kind getKind() { return kind; }
    /** @return entity protected by the layer */
    public LivingEntity getTarget() { return target; }
    /** @return granting entity UUID, or {@code null} when no source was recorded */
    public UUID getSourceId() { return sourceId; }
    /** @return layer UUID, useful when multiple layers share one key */
    public UUID getLayerId() { return layerId; }
    /** @return configured layer key used by mechanics and filters */
    public String getKey() { return key; }
    /** @return capacity involved in the completed transition */
    public double getAmount() { return amount; }
    /** @return live capacity after the completed transition */
    public double getRemaining() { return remaining; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

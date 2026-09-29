package com.sucy.skill.dynamic.signal;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One synchronous delivery to one active skill owner.
 *
 * <p>The manager validates the channel contract before constructing this
 * event. The argument map is copied and exposed as an unmodifiable map so an
 * earlier receiver cannot mutate the values observed by a later receiver. The
 * dynamic trigger temporarily projects these values into the recipient's
 * signal namespace and removes them when execution returns; signal parameters
 * therefore do not become persistent cast data.</p>
 */
public final class SignalEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String channel;
    private final LivingEntity sender;
    private final LivingEntity recipient;
    private final Map<String, Object> arguments;

    /**
     * Creates one delivery record. This constructor does not perform contract
     * validation; callers must use {@link SignalManager#emit} or
     * {@link SignalManager#arguments} for validated dispatch.
     *
     * @param channel globally registered signal channel
     * @param sender entity that initiated the signal
     * @param recipient entity whose trigger tree receives it
     * @param arguments validated, scalar signal parameters
     */
    public SignalEvent(String channel, LivingEntity sender, LivingEntity recipient,
                       Map<String, Object> arguments) {
        this.channel = channel;
        this.sender = sender;
        this.recipient = recipient;
        this.arguments = Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
    }

    /** @return channel name after contract normalization */
    public String getChannel() { return channel; }
    /** @return entity that emitted the signal */
    public LivingEntity getSender() { return sender; }
    /** @return entity whose signal trigger is being executed */
    public LivingEntity getRecipient() { return recipient; }
    /** @return immutable validated argument snapshot for this delivery */
    public Map<String, Object> getArguments() { return arguments; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

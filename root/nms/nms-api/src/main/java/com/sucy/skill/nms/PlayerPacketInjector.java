package com.sucy.skill.nms;

import org.bukkit.entity.Player;

/**
 * Player-scoped packet hook used by combo handling. Keeping this as an
 * interface prevents listener code from knowing which packet class names or
 * channel fields exist on a specific Minecraft core.
 */
public interface PlayerPacketInjector {
    /**
     * @return true when the injector can attach to players on this server
     */
    boolean isWorking();

    /**
     * Adds packet interception for a player.
     *
     * @param player player to attach
     */
    void addPlayer(Player player);

    /**
     * Removes packet interception for a player.
     *
     * @param player player to detach
     */
    void removePlayer(Player player);
}


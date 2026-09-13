package com.sucy.skill.packet;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.KeyPressEvent;
import com.sucy.skill.nms.NmsProvider;
import com.sucy.skill.nms.PlayerPacketInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * SkillAPI © 2018
 * com.sucy.skill.PacketInjector
 *
 * Public-facing packet injector wrapper. The actual channel/NMS work lives in
 * version modules so this package can keep the listener API stable across all
 * supported Paper core versions.
 */
public class PacketInjector implements PlayerPacketInjector {
    private final PlayerPacketInjector delegate;

    /**
     * Sets up the injector for the running core version.
     */
    public PacketInjector(final SkillAPI skillAPI) {
        this.delegate = NmsProvider.bridge().createPacketInjector(
                skillAPI,
                (player, keyName) -> SkillAPI.schedule(() -> {
                    if (keyName == null) return;
                    try {
                        // Packet/NMS adapters provide raw names; validate them
                        // before dispatching so a protocol mismatch cannot
                        // throw from the scheduler thread.
                        KeyPressEvent.Key key = KeyPressEvent.Key.valueOf(keyName.toUpperCase());
                        Bukkit.getPluginManager().callEvent(new KeyPressEvent(player, key));
                    } catch (IllegalArgumentException ignored) {
                        // Unknown key names are ignored for this server version.
                    }
                }, 0));
    }

    @Override
    public boolean isWorking() {
        return delegate.isWorking();
    }

    /**
     * Injects an interceptor to the player's network manager
     *
     * @param p player to add to
     */
    @Override
    public void addPlayer(Player p) {
        delegate.addPlayer(p);
    }

    /**
     * Removes an interceptor from a player's network manager
     *
     * @param p player to remove from
     */
    @Override
    public void removePlayer(Player p) {
        delegate.removePlayer(p);
    }
}

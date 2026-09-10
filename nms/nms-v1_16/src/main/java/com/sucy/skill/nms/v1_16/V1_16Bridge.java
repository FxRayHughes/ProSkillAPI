package com.sucy.skill.nms.v1_16;

import com.sucy.skill.nms.v1_13.V1_13Bridge;
import org.bukkit.entity.Player;

/**
 * Bridge for the 1.16 generation.
 *
 * <p>Delta from 1.13: Paper started shipping Adventure, and the Spigot
 * BungeeCord-chat bridge began its slow path to removal. Adventure is used when
 * the running core actually has it, which is a per-core question rather than a
 * per-version one — the same 1.16 release exists as Spigot without Adventure —
 * so the check is a runtime probe and Spigot simply keeps the inherited
 * path.</p>
 */
public class V1_16Bridge extends V1_13Bridge {
    private final AdventureText adventure = new AdventureText();

    @Override
    public String id() {
        return "v1_16";
    }

    @Override
    public boolean isActionBarSupported() {
        return adventure.isAvailable() || super.isActionBarSupported();
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        if (adventure.isAvailable() && adventure.sendActionBar(player, message)) {
            return true;
        }
        return super.sendActionBar(player, message);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        if (adventure.isAvailable()
                && adventure.sendTitle(player, title, subtitle, fadeIn, duration, fadeOut)) {
            return;
        }
        super.sendTitle(player, title, subtitle, fadeIn, duration, fadeOut);
    }
}

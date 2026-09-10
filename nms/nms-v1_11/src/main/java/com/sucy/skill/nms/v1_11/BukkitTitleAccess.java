package com.sucy.skill.nms.v1_11;

import com.sucy.skill.nms.v1_8.TitleAccess;
import org.bukkit.entity.Player;

/**
 * Titles through the public {@code Player#sendTitle} overload added in 1.11.
 *
 * <p>Preferring the API over the packet removes the hand-built JSON component
 * and the two obfuscated inner-class lookups the packet path needs.</p>
 */
public class BukkitTitleAccess extends TitleAccess {
    @Override
    public void send(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        try {
            player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
        } catch (Throwable ignored) {
            // Fall back to the packet path on cores that only partly ship it.
            super.send(player, title, subtitle, fadeIn, duration, fadeOut);
        }
    }
}

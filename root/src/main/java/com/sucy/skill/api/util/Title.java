package com.sucy.skill.api.util;

import com.sucy.skill.nms.NmsProvider;
import org.bukkit.entity.Player;

/**
 * Sends title packets through the version bridge. The class remains as the
 * public utility entry point used by managers while NMS details move to modules.
 */
public class Title
{
    public static void send(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut)
    {
        NmsProvider.bridge().sendTitle(player, title, subtitle, fadeIn, duration, fadeOut);
    }
}

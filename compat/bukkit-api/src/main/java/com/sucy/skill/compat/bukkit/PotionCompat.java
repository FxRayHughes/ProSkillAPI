/**
 * SkillAPI
 * com.sucy.skill.compat.bukkit.PotionCompat
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.compat.bukkit;

import org.bukkit.potion.PotionEffectType;

/**
 * Centralizes potion-effect aliases that changed between Bukkit/Paper lines.
 * Consumers should not reference renamed constants directly because older
 * servers may not expose the modern field names at class-load time.
 */
public final class PotionCompat {
    public static final PotionEffectType SLOWNESS = find("SLOWNESS", "SLOW");

    private PotionCompat() {
        // Utility class: potion effect instances are supplied by the active server.
    }

    /**
     * Uses name lookup instead of enum/static fields so missing aliases simply
     * return null on unsupported cores instead of breaking plugin enable.
     */
    private static PotionEffectType find(String... names) {
        for (String name : names) {
            PotionEffectType type = PotionEffectType.getByName(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }
}

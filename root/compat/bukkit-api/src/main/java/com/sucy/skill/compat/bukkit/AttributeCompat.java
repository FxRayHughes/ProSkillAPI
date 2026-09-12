/**
 * SkillAPI
 * com.sucy.skill.compat.bukkit.AttributeCompat
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.compat.bukkit;

import org.bukkit.attribute.Attribute;

/**
 * Resolves Bukkit attributes across legacy GENERIC_* names and modern Paper
 * names. Attribute enum names changed across core lines, so callers must treat
 * a null value as "not supported by this server" instead of hard-linking enum
 * constants from one API version.
 */
public final class AttributeCompat {
    public static final Attribute MAX_HEALTH = find("MAX_HEALTH", "GENERIC_MAX_HEALTH");
    public static final Attribute ATTACK_SPEED = find("ATTACK_SPEED", "GENERIC_ATTACK_SPEED");
    public static final Attribute ARMOR = find("ARMOR", "GENERIC_ARMOR");
    public static final Attribute LUCK = find("LUCK", "GENERIC_LUCK");
    public static final Attribute KNOCKBACK_RESISTANCE = find("KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE");
    public static final Attribute ARMOR_TOUGHNESS = find("ARMOR_TOUGHNESS", "GENERIC_ARMOR_TOUGHNESS");

    private AttributeCompat() {
        // Utility class: the resolved Attribute instances are immutable Bukkit values.
    }

    /**
     * Tries modern Paper names first and legacy Bukkit names second so one
     * binary can run on every supported core without enum linkage failures.
     */
    private static Attribute find(String... names) {
        for (String name : names) {
            try {
                return Attribute.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // Continue through the known aliases for this logical attribute.
            }
        }
        return null;
    }
}

package com.sucy.skill.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import java.util.Locale;

/** Resolves config materials using the server's own flattening table and legacy data. */
public final class MaterialCompat {
    private MaterialCompat() { }

    /** Legacy metadata is only applicable before flattening, never to a modern material. */
    public static boolean isFlattened() {
        return Material.getMaterial("OAK_PLANKS") != null;
    }

    /** Returns null for unsupported names; item priority distinguishes item and block aliases. */
    public static Material resolve(String name, int data, boolean item) {
        if (name == null) return null;
        String key = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (key.startsWith("MINECRAFT:")) key = key.substring(10);
        Material direct = Material.getMaterial(key);
        if (!isFlattened()) return direct;
        if (direct != null && !direct.name().startsWith("LEGACY_")) return direct;
        // Sign flattening changed again in 1.14; some server conversion tables
        // return AIR for these legacy names, which must not become a block filter.
        String alias = key.startsWith("LEGACY_") ? key.substring(7) : key;
        if (alias.equals("WALL_SIGN")) {
            Material sign = Material.getMaterial("OAK_WALL_SIGN");
            if (sign != null) return sign;
        }
        if (alias.equals("SIGN_POST") || alias.equals("SIGN")) {
            Material sign = Material.getMaterial("OAK_SIGN");
            if (sign != null) return sign;
        }
        Material legacy = direct != null ? direct : Material.getMaterial("LEGACY_" + key);
        // The Bukkit table preserves variants such as WOOL:14 -> RED_WOOL.
        Material result = legacy == null ? null : Bukkit.getUnsafe().fromLegacy(new MaterialData(legacy, (byte) data), item);
        return result == Material.AIR && !alias.equals("AIR") ? null : result;
    }
}

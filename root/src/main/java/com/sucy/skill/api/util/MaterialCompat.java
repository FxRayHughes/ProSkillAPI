package com.sucy.skill.api.util;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import java.lang.reflect.Method;
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
        // Flattened versions no longer retain the legacy color byte. Resolve
        // all legacy dyeable names before XMaterial sees them; otherwise
        // XMaterial must choose one default variant and the configured color
        // is lost. The order follows the pre-1.13 dye data contract.
        String legacyColor = legacyColorVariant(name, data);
        if (legacyColor != null) {
            name = legacyColor;
            data = 0;
        }
        // XMaterial is the canonical cross-version table for non-player items.
        Material xMaterial = XMaterial.matchXMaterial(name).map(XMaterial::parseMaterial).orElse(null);
        if (xMaterial != null) return xMaterial;
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

    /** Maps old dyeable material data to the corresponding flattened material. */
    private static String legacyColorVariant(String name, int data) {
        if (!isFlattened() || data < 0 || data > 15 || name == null) return null;
        String key = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (key.startsWith("MINECRAFT:")) key = key.substring(10);
        if ("INK_SACK".equals(key) || "DYE".equals(key)) {
            final String[] dyes = {
                    "BLACK_DYE", "RED_DYE", "GREEN_DYE", "BROWN_DYE", "BLUE_DYE", "PURPLE_DYE",
                    "CYAN_DYE", "LIGHT_GRAY_DYE", "GRAY_DYE", "PINK_DYE", "LIME_DYE", "YELLOW_DYE",
                    "LIGHT_BLUE_DYE", "MAGENTA_DYE", "ORANGE_DYE", "WHITE_DYE"
            };
            return dyes[data];
        }
        String suffix;
        switch (key) {
            case "WOOL": suffix = "WOOL"; break;
            case "STAINED_GLASS": suffix = "STAINED_GLASS"; break;
            case "STAINED_GLASS_PANE": suffix = "STAINED_GLASS_PANE"; break;
            case "CARPET": suffix = "CARPET"; break;
            case "CONCRETE": suffix = "CONCRETE"; break;
            case "CONCRETE_POWDER": suffix = "CONCRETE_POWDER"; break;
            case "HARDENED_CLAY":
            case "TERRACOTTA": suffix = "TERRACOTTA"; break;
            case "BANNER":
            case "STANDING_BANNER": suffix = "BANNER"; break;
            default: return null;
        }
        final String[] colors = {
                "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME",
                "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE",
                "BROWN", "GREEN", "RED", "BLACK"
        };
        return colors[data] + "_" + suffix;
    }

    /**
     * Applies modern custom-model metadata without linking the method into the
     * main plugin class on 1.12.2, where ItemMeta does not declare it.
     */
    public static boolean setCustomModelData(ItemMeta meta, int data) {
        if (meta == null) return false;
        try {
            Method method = ItemMeta.class.getMethod("setCustomModelData", Integer.class);
            method.invoke(meta, data);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    /** Reads custom-model metadata when supported by the running server. */
    public static Integer getCustomModelData(ItemMeta meta) {
        if (meta == null) return null;
        try {
            Method has = ItemMeta.class.getMethod("hasCustomModelData");
            if (!Boolean.TRUE.equals(has.invoke(meta))) return null;
            Method get = ItemMeta.class.getMethod("getCustomModelData");
            Object value = get.invoke(meta);
            return value instanceof Number ? ((Number) value).intValue() : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}

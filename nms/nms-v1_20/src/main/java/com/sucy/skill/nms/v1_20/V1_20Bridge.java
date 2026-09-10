package com.sucy.skill.nms.v1_20;

import com.sucy.skill.nms.NmsCapabilities;
import com.sucy.skill.nms.v1_17.V1_17Bridge;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

/**
 * Bridge for the 1.20 generation.
 *
 * <p>Delta from 1.17: 1.20.5 replaced item NBT with data components, and with
 * it the way "no attribute modifiers" is expressed. {@code HIDE_ATTRIBUTES}
 * alone no longer suppresses the vanilla damage numbers on a component-based
 * item, so where the API offers an explicit empty modifier set this bridge sets
 * it and only falls back to the flag otherwise.</p>
 */
public class V1_20Bridge extends V1_17Bridge {
    private static final Method SET_ATTRIBUTE_MODIFIERS = NmsCapabilities.findMethod(
            ItemMeta.class, "setAttributeModifiers", com.google.common.collect.Multimap.class);

    @Override
    public String id() {
        return "v1_20";
    }

    @Override
    public ItemStack removeAttackDmg(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemStack result = item.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            return result;
        }
        if (result.getType().getMaxDurability() > 0) {
            meta.setUnbreakable(true);
            addFlag(meta, "HIDE_UNBREAKABLE");
        }
        if (!clearModifiers(meta)) {
            addFlag(meta, "HIDE_ATTRIBUTES");
        }
        result.setItemMeta(meta);
        return result;
    }

    /**
     * @param meta metadata to strip
     * @return true when an explicit empty modifier set was applied
     */
    protected boolean clearModifiers(ItemMeta meta) {
        if (SET_ATTRIBUTE_MODIFIERS == null) {
            return false;
        }
        try {
            // An empty multimap is not the same as null here: null means
            // "use the material defaults", which is what we are removing.
            SET_ATTRIBUTE_MODIFIERS.invoke(
                    meta, com.google.common.collect.ImmutableMultimap.of());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}

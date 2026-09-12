package com.sucy.skill.nms.v1_8;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Hides the vanilla attack damage tooltip by clearing the item's attribute
 * modifiers in raw NBT.
 *
 * <p>Before 1.11 there is no {@code ItemMeta#setUnbreakable}, so the unbreakable
 * flag and the {@code HideFlags} bitmask have to be written to NBT as well.
 * {@code V1_11Bridge} swaps in a subclass that uses the public API for that
 * half once it exists.</p>
 */
public class DamageLoreAccess {
    protected Class<?> nbtBase;
    protected Class<?> nbtCompound;
    protected Class<?> nbtList;
    protected Class<?> nmsItem;
    protected Method set;
    protected Method setTag;
    protected Method setBool;
    protected Method setInt;
    protected Method getTag;
    protected Method asCraft;
    protected Method asNms;

    public DamageLoreAccess() {
        try {
            nbtBase = LegacyReflection.nmsClass("NBTBase");
            nbtCompound = LegacyReflection.nmsClass("NBTTagCompound");
            nbtList = LegacyReflection.nmsClass("NBTTagList");
            nmsItem = LegacyReflection.nmsClass("ItemStack");
            Class<?> craftItem = LegacyReflection.craftClass("inventory.CraftItemStack");
            asNms = craftItem.getMethod("asNMSCopy", ItemStack.class);
            getTag = nmsItem.getMethod("getTag");
            set = nbtCompound.getMethod("set", String.class, nbtBase);
            setTag = nmsItem.getMethod("setTag", nbtCompound);
            setBool = nbtCompound.getMethod("setBoolean", String.class, boolean.class);
            setInt = nbtCompound.getMethod("setInt", String.class, int.class);
            asCraft = craftItem.getMethod("asCraftMirror", nmsItem);
        } catch (Exception ignored) {
            Bukkit.getLogger().warning(
                    "[SkillAPI] Failed to set up reflection for removing damage lores");
            nbtBase = null;
        }
    }

    /**
     * @param item source item
     * @return copy with the vanilla damage tooltip suppressed
     */
    public ItemStack remove(ItemStack item) {
        if (nbtBase == null) {
            return item;
        }
        try {
            ItemStack copy = item.clone();
            Object nmsStack = asNms.invoke(null, copy);
            Object nbt = getTag.invoke(nmsStack);
            if (nbt == null) {
                nbt = nbtCompound.getConstructor().newInstance();
            }
            applyUnbreakable(copy, nbt);
            set.invoke(nbt, "AttributeModifiers", nbtList.getConstructor().newInstance());
            setTag.invoke(nmsStack, nbt);
            return (ItemStack) asCraft.invoke(null, nmsStack);
        } catch (Exception ignored) {
            return item;
        }
    }

    /**
     * Writes the unbreakable state. Overridden from 1.11 onward, where the
     * public {@code ItemMeta} API covers this without NBT.
     */
    protected void applyUnbreakable(ItemStack item, Object nbt) throws Exception {
        if (item.getType().getMaxDurability() > 0) {
            setBool.invoke(nbt, "Unbreakable", true);
            setInt.invoke(nbt, "HideFlags", 4);
        }
    }
}

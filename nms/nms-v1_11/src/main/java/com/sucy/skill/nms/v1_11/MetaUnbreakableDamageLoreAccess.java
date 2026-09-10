package com.sucy.skill.nms.v1_11;

import com.sucy.skill.nms.v1_8.DamageLoreAccess;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Sets the unbreakable state through {@code ItemMeta} instead of raw NBT.
 *
 * <p>The attribute-modifier clearing is still done by the inherited NBT path:
 * {@code HIDE_ATTRIBUTES} only hides vanilla modifiers from the tooltip, while
 * SkillAPI needs them actually removed so they stop contributing damage.</p>
 */
public class MetaUnbreakableDamageLoreAccess extends DamageLoreAccess {
    @Override
    protected void applyUnbreakable(ItemStack item, Object nbt) throws Exception {
        if (item.getType().getMaxDurability() <= 0) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            super.applyUnbreakable(item, nbt);
            return;
        }
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
    }
}

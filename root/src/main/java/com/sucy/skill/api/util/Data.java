/**
 * SkillAPI
 * com.sucy.skill.api.util.Data
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.api.util;

import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.SkillAPI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing loading/saving certain data
 */
public class Data {
    private static final String MAT        = "icon";
    private static final String DATA       = "icon-data";
    private static final String DURABILITY = "icon-durability";
    private static final String LORE       = "icon-lore";

    private static ItemStack parse(final String mat, final short dur, final int data, final List<String> lore) {
        try {
            // Resolve legacy color/data through the shared material boundary;
            // this preserves stained-glass colors on flattened servers.
            final Material resolved = MaterialCompat.resolve(mat, data, true);
            final ItemStack item = resolved == null
                    ? XMaterial.JACK_O_LANTERN.parseItem()
                    : new ItemStack(resolved);
            final Material material = item.getType();
            final ItemMeta meta = item.getItemMeta();
            if (SkillAPI.getSettings().useGUIModelData()) {
                if (data!=0) {
                    MaterialCompat.setCustomModelData(meta, data);
                }
            } else if (!MaterialCompat.isFlattened()) {
                item.setData(new MaterialData(material, (byte) data));
            }
            if (lore != null && !lore.isEmpty()) {
                final List<String> colored = TextFormatter.colorStringList(lore);
                meta.setDisplayName(colored.remove(0));
                meta.setLore(colored);
            }
            if (SkillAPI.getSettings().useOldDurability()) {
                item.setItemMeta(meta);
                item.setDurability(dur);
            } else {
                if (meta instanceof Damageable) {
                    ((Damageable) meta).setDamage(dur);
                }
                item.setItemMeta(meta);
            }
            // Legacy non-tools use durability as their variant, not wear damage.
            if (!MaterialCompat.isFlattened() && material.getMaxDurability() == 0) {
                item.setDurability((short) data);
            }
            return DamageLoreRemover.removeAttackDmg(item);
        } catch (final Exception ex) {
            return XMaterial.JACK_O_LANTERN.parseItem();
        }
    }

    /**
     * Serializes an item icon into a configuration
     *
     * @param item   item to serialize
     * @param config config to serialize into
     */
    public static void serializeIcon(ItemStack item, DataSection config) {
        // Legacy Bukkit may return a null meta for malformed/unsupported icon
        // data; preserve the material while avoiding startup-wide failure.
        if (item == null) item = XMaterial.JACK_O_LANTERN.parseItem();
        config.set(MAT, item.getType().name());

        ItemMeta meta = item.getItemMeta();
        if (SkillAPI.getSettings().useGUIModelData()) {
            Integer modelData = MaterialCompat.getCustomModelData(meta);
            config.set(DATA, modelData == null ? 0 : modelData);
        } else {
            config.set(DATA, item.getData().getData());
        }

        if (SkillAPI.getSettings().useOldDurability()) {
            config.set(DURABILITY, item.getDurability());
        } else {
            if (meta instanceof Damageable) config.set(DURABILITY, ((Damageable) meta).getDamage());
            else config.set(DURABILITY, 0);
        }

        if (meta != null && meta.hasDisplayName()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore == null) { lore = new ArrayList<>(); }
            lore.add(0, item.getItemMeta().getDisplayName());
            int count = lore.size();
            for (int i = 0; i < count; i++) { lore.add(lore.remove(0).replace(ChatColor.COLOR_CHAR, '&').replaceAll("attr:&"+".", "attr:")); }
            config.set(LORE, lore);
        }
    }

    /**
     * Parses an item icon from a configuration
     *
     * @param config config to load from
     *
     * @return parsed item icon or a plain Jack O' Lantern if invalid
     */
    public static ItemStack parseIcon(DataSection config) {
        if (config == null) {
            return XMaterial.JACK_O_LANTERN.parseItem();
        }

        final int data = config.getInt(DATA, 0);
        return parse(
                config.getString(MAT, "JACK_O_LANTERN"),
                (short) config.getInt(DURABILITY, data),
                data,
                ConfigValues.strings(config.getList(LORE, null)));
    }
}

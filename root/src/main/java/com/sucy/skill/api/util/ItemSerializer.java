/**
 * SkillAPI
 * com.sucy.skill.api.util.ItemSerializer
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.serialization.SerializationProvider;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Inventory serializer used for combat item restore data. The public methods
 * keep the historical names because they are part of the persisted data path,
 * but current writes are delegated to versioned serialization modules.
 */
public class ItemSerializer {

    public static String toBase64(ItemStack[] items) {
        if (items == null) return null;

        String serialized = SerializationProvider.service().serialize(items);
        return serialized == null ? basicSerialize(items) : serialized;
    }

    public static ItemStack[] fromBase64(String data) {
        if (data == null) return null;

        if (data.indexOf(';') >= 0) {
            return basicDeserialize(data);
        }

        // New modules own complete item serialization and legacy module readers.
        // If no module can read the data, return null instead of corrupting slots.
        return SerializationProvider.service().deserialize(data);
    }

    /**
     * Basic legacy format kept as a compatibility fallback. It intentionally
     * stores only the original fields this plugin knew how to restore before
     * full NBT support was available.
     */
    private static String basicSerialize(ItemStack[] items)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(items.length);
        builder.append(';');
        for (int i = 0; i < items.length; i++)
        {
            ItemStack is = items[i];
            if (is != null)
            {
                builder.append(i);
                builder.append('#');

                if (VersionManager.isVersionAtLeast(11605))
                {
                    String isType = String.valueOf(is.getType());
                    builder.append("t@");
                    builder.append(isType);
                } else
                {
                    String isType = String.valueOf(is.getType().getId());
                    builder.append("t@");
                    builder.append(isType);
                }

                if (is.getDurability() != 0)
                {
                    String isDurability = String.valueOf(is.getDurability());
                    builder.append(":d@");
                    builder.append(isDurability);
                }

                if (is.getAmount() != 1)
                {
                    String isAmount = String.valueOf(is.getAmount());
                    builder.append(":a@");
                    builder.append(isAmount);
                }

                Map<Enchantment,Integer> isEnch = is.getEnchantments();
                if (isEnch.size() > 0)
                {
                    for (Map.Entry<Enchantment,Integer> ench : isEnch.entrySet())
                    {
                        builder.append(":e@");
                        builder.append(ENCHANT_IDS.get(ench.getKey().getName()));
                        builder.append('@');
                        builder.append(ench.getValue());
                    }
                }

                ItemMeta meta = is.getItemMeta();
                if (meta.hasDisplayName()) {
                    builder.append(":n@");
                    builder.append(meta.getDisplayName().replaceAll("[:@#;]", ""));
                }

                if (meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        builder.append(":l@");
                        builder.append(line.replaceAll("[:;@#]", ""));
                    }
                }

                builder.append(';');
            }
        }
        return builder.toString();
    }

    private static ItemStack[] basicDeserialize(String invString)
    {
        if (invString == null || invString.trim().isEmpty()) return null;
        try {
            String[] serializedBlocks = invString.split(";");
            if (serializedBlocks.length == 0)
                return null;
            String invInfo = serializedBlocks[0];
            ItemStack[] deserializedInventory = new ItemStack[Integer.valueOf(invInfo)];

        for (int i = 1; i <= deserializedInventory.length && i < serializedBlocks.length; i++)
        {
            String[] serializedBlock = serializedBlocks[i].split("#");
            if (serializedBlock.length < 2) continue;
            int stackPosition = Integer.valueOf(serializedBlock[0]);

            if (stackPosition >= deserializedInventory.length)
            {
                continue;
            }

            ItemStack is = null;
            Boolean createdItemStack = false;

            String[] serializedItemStack = serializedBlock[1].split(":");
            for (String itemInfo : serializedItemStack)
            {
                String[] itemAttribute = itemInfo.split("@");
                if (itemAttribute[0].equals("t"))
                {
                    if (VersionManager.isVersionAtLeast(11605))
                    {
                        String id = String.valueOf(itemAttribute[1]);
                        final Material mat = Material.getMaterial(id);
                        is = new ItemStack(mat);
                        createdItemStack = true;
                    } else
                    {
                        int id = Integer.valueOf(itemAttribute[1]);
                        if (id >= 2256) id -= 2267 - Material.values().length;
                        final Material mat = Material.values()[id];
                        is = new ItemStack(mat);
                        createdItemStack = true;
                    }
                }
                else if (itemAttribute[0].equals("d") && createdItemStack)
                {
                    is.setDurability(Short.valueOf(itemAttribute[1]));
                }
                else if (itemAttribute[0].equals("a") && createdItemStack)
                {
                    is.setAmount(Integer.valueOf(itemAttribute[1]));
                }
                else if (itemAttribute[0].equals("e") && createdItemStack)
                {
                    final String name = ENCHANT_IDS.inverse().getOrDefault(Integer.valueOf(itemAttribute[1]), "OXYGEN");
                    is.addUnsafeEnchantment(Enchantment.getByName(name), Integer.valueOf(itemAttribute[2]));
                }
                else if (itemAttribute[0].equals("n") && createdItemStack)
                {
                    ItemMeta meta = is.getItemMeta();
                    meta.setDisplayName(itemAttribute[1]);
                    is.setItemMeta(meta);
                }
                else if (itemAttribute[0].equals("l") && createdItemStack)
                {
                    ItemMeta meta = is.getItemMeta();
                    List<String> lore = meta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.add(itemAttribute[1]);
                    meta.setLore(lore);
                    is.setItemMeta(meta);
                }
            }
            deserializedInventory[stackPosition] = is;
        }

            return deserializedInventory;
        } catch (RuntimeException ex) {
            // Legacy inventory strings are user/database input. A corrupt
            // token must not abort player-data initialization or the server
            // reload; callers can treat a null result as an empty inventory.
            return null;
        }
    }

    private static final BiMap<String, Integer> ENCHANT_IDS = ImmutableBiMap.<String, Integer>builder()
            .put("PROTECTION_ENVIRONMENTAL", 0)
            .put("PROTECTION_FIRE", 1)
            .put("PROTECTION_FALL", 2)
            .put("PROTECTION_EXPLOSIONS", 3)
            .put("PROTECTION_PROJECTILE", 4)
            .put("OXYGEN", 5)
            .put("WATER_WORKER", 6)
            .put("THORNS", 7)
            .put("DEPTH_STRIDER", 8)
            .put("FROST_WALKER", 9)
            .put("BINDING_CURSE", 10)
            .put("DAMAGE_ALL", 16)
            .put("DAMAGE_UNDEAD", 17)
            .put("DAMAGE_ARTHROPODS", 18)
            .put("KNOCKBACK", 19)
            .put("FIRE_ASPECT", 20)
            .put("LOOT_BONUS_MOBS", 21)
            .put("SWEEPING_EDGE", 22)
            .put("DIG_SPEED", 32)
            .put("SILK_TOUCH", 33)
            .put("DURABILITY", 34)
            .put("LOOT_BONUS_BLOCKS", 35)
            .put("ARROW_DAMAGE", 48)
            .put("ARROW_KNOCKBACK", 49)
            .put("ARROW_FIRE", 50)
            .put("ARROW_INFINITE", 51)
            .put("LUCK", 61)
            .put("LURE", 62)
            .put("MENDING", 70)
            .put("VANISHING_CURSE", 71)
            .put("LOYALTY", 80)
            .put("IMPALING", 81)
            .put("RIPTIDE", 82)
            .put("CHANNELING", 83)
            .build();
}

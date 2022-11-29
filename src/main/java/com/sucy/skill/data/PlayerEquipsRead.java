package com.sucy.skill.data;

import com.rit.sucy.config.parse.NumberParser;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.event.PlayerReadAttributeEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEquipsRead {

    public static Settings settings = SkillAPI.getSettings();

    public static boolean canUse(PlayerData playerData, List<String> lores, String name) {
        if (SkillAPI.getSettings().isCheckLore() && !useClass(playerData, lores)) {
            playerData.getPlayer().sendMessage("§c" + name + " §4所需职业不符");
            return false;
        }
        if (!useLevel(playerData, lores)) {
            playerData.getPlayer().sendMessage("§c" + name + " §4所需等级不符");
            return false;
        }
        if (settings.isCheckAttributes() && !useAttribute(playerData, lores)) {
            playerData.getPlayer().sendMessage("§c" + name + " §4所需属性不符");
            return false;
        }
        return true;
    }

    public static boolean useClass(PlayerData playerData, List<String> lores) {
        String lct = SkillAPI.getSettings().getLoreClassText();
        for (String lore : lores) {
            String lower = ChatColor.stripColor(lore).toLowerCase();
            if (lower.contains(lct)) {
                if (playerData.getMainClass() == null || playerData.getMainClass().getData() == null) {
                    return true;
                }
                String className = playerData.getMainClass().getData().getName();
                return lower.contains(className);
            }
        }
        return true;
    }

    public static boolean useLevel(PlayerData playerData, List<String> lores) {
        String lct = SkillAPI.getSettings().getLoreLevelText();
        for (String lore : lores) {
            String lower = ChatColor.stripColor(lore).toLowerCase();
            if (lower.contains(lct)) {
                String[] gett = lct.split("[: ]");
                int si = NumberParser.parseInt(gett[gett.length - 1]);
                return playerData.getMainClass().getLevel() >= si;
            }
        }
        return true;
    }

    public static boolean useAttribute(PlayerData playerData, List<String> lores) {
        for (String lore : lores) {
            String lower = ChatColor.stripColor(lore).toLowerCase();
            for (String attr : SkillAPI.getAttributeManager().getLookupKeys()) {
                String text = settings.getAttrReqText(attr);
                if (lower.startsWith(text)) {
                    String normalized = SkillAPI.getAttributeManager().normalize(attr);
                    if (AttributeAPI.getAttribute(playerData.getPlayer(), normalized) < NumberParser.parseInt(lower.substring(text.length()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void update(PlayerData playerData) {
        if (!SkillAPI.getSettings().isCheckAttributes()) {
            return;
        }
        PlayerInventory playerInventory = playerData.getPlayer().getInventory();
        for (int slot : SkillAPI.getSettings().getSlots()) {
            ItemStack itemStack = playerInventory.getItem(slot);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    String name = itemStack.getItemMeta().getDisplayName();
                    if (name.equals("null")) {
                        name = itemStack.getType().name();
                    }
                    if (!canUse(playerData, lore,name )) {
                        return;
                    }
                    ConcurrentHashMap<String, Integer> attribs = new ConcurrentHashMap<>();
                    assert lore != null;
                    for (String line : lore) {
                        String lower = ChatColor.stripColor(line).toLowerCase();
                        Pair<String, Integer> attrs = PlayerEquipsUtils.getAttribute(lower);
                        if (attrs != null) {
                            PlayerReadAttributeEvent event = new PlayerReadAttributeEvent(playerData, itemStack, attrs.getKey(), attrs.getLast());
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                if (attribs.containsKey(event.getAttribute())) {
                                    attribs.put(event.getAttribute(), attribs.get(event.getAttribute()) + event.getValue());
                                } else {
                                    attribs.put(event.getAttribute(), event.getValue());
                                }
                            }
                        }
                    }
                    playerData.addAttrib.put("Equip_" + slot, attribs);
                } else {
                    playerData.addAttrib.put("Equip_" + slot, new ConcurrentHashMap<>());
                }
            } else {
                playerData.addAttrib.put("Equip_" + slot, new ConcurrentHashMap<>());
            }
        }
        updateHand(playerData, false);
    }

    public static void updateHand(PlayerData playerData, boolean off) {
        if (!SkillAPI.getSettings().isCheckAttributes()) {
            return;
        }
        ItemStack itemStack;
        String key;
        if (off) {
            itemStack = playerData.getPlayer().getInventory().getItemInOffHand();
            key = "OFF";
        } else {
            itemStack = playerData.getPlayer().getInventory().getItemInMainHand();
            key = "MAIN";
        }
        if (itemStack.getType() == Material.AIR || !itemStack.hasItemMeta()) {
            playerData.addAttrib.put("Equip_" + key, new ConcurrentHashMap<>());
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.hasLore()) {
            playerData.addAttrib.put("Equip_" + key, new ConcurrentHashMap<>());
            return;
        }
        List<String> lore = meta.getLore();
        if (!canUse(playerData, lore, itemStack.getItemMeta().getDisplayName())) {
            return;
        }
        ConcurrentHashMap<String, Integer> attribs = new ConcurrentHashMap<>();
        assert lore != null;
        for (String line : lore) {
            String lower = ChatColor.stripColor(line).toLowerCase();
            Pair<String, Integer> attrs = PlayerEquipsUtils.getAttribute(lower);
            if (attrs != null) {
                PlayerReadAttributeEvent event = new PlayerReadAttributeEvent(playerData, itemStack, attrs.getKey(), attrs.getLast());
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    if (attribs.containsKey(event.getAttribute())) {
                        attribs.put(event.getAttribute(), attribs.get(event.getAttribute()) + event.getValue());
                    } else {
                        attribs.put(event.getAttribute(), event.getValue());
                    }
                }
            }
        }
        playerData.addAttrib.put("Equip_" + key, attribs);
    }


}
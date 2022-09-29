package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SwapHandItemsTrigger implements Trigger<PlayerSwapHandItemsEvent> {

    @Override
    public String getKey() {
        return "SWAP_HAND";
    }

    @Override
    public Class<PlayerSwapHandItemsEvent> getEvent() {
        return PlayerSwapHandItemsEvent.class;
    }

    @Override
    public boolean shouldTrigger(final PlayerSwapHandItemsEvent event, final int level, final Settings settings) {
        if (settings.getBool("sneaking", false)) {
            if (!event.getPlayer().isSneaking()) {
                return false;
            }
        }
        if (settings.getBool("main_enable", false)) {
            ItemStack itemStack = event.getMainHandItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return false;
            }
            String name = settings.getString("main_name");
            if (!name.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    return false;
                }
                String cname = meta.getDisplayName();
                if (!cname.contains(name)) {
                    return false;
                }
            }
            String lore = settings.getString("main_lore");
            if (!lore.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return false;
                }
                List<String> clone = meta.getLore();
                if (clone == null || clone.isEmpty() || !meta.hasLore()) {
                    return false;
                }
                return clone.stream().noneMatch(i ->
                        ChatColor.stripColor(i).contains(lore)
                );
            }
        }
        if (settings.getBool("off_enable", false)) {
            ItemStack itemStack = event.getOffHandItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return false;
            }
            String name = settings.getString("off_name");
            if (!name.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    return false;
                }
                String cname = meta.getDisplayName();
                if (!cname.contains(name)) {
                    return false;
                }
            }
            String lore = settings.getString("off_lore");
            if (!lore.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return false;
                }
                List<String> clone = meta.getLore();
                if (clone == null || clone.isEmpty() || !meta.hasLore()) {
                    return false;
                }
                return clone.stream().noneMatch(i ->
                        ChatColor.stripColor(i).contains(lore)
                );
            }
        }
        event.setCancelled(settings.getBool("cancelled", false));
        return true;
    }

    @Override
    public void setValues(final PlayerSwapHandItemsEvent event, final Map<String, Object> data) {
        data.put("api-item-type", Objects.requireNonNull(event.getMainHandItem().getType().name()));
    }

    @Override
    public LivingEntity getCaster(final PlayerSwapHandItemsEvent event) {
        return event.getPlayer();
    }

    @Override
    public LivingEntity getTarget(final PlayerSwapHandItemsEvent event, final Settings settings) {
        return event.getPlayer();
    }
}

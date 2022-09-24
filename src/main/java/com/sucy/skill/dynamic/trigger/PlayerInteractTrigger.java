package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;


public class PlayerInteractTrigger implements Trigger<PlayerInteractEvent> {

    @Override
    public String getKey() {
        return "INTERACT";
    }

    @Override
    public Class<PlayerInteractEvent> getEvent() {
        return PlayerInteractEvent.class;
    }

    @Override
    public boolean shouldTrigger(final PlayerInteractEvent event, final int level, final Settings settings) {
        ItemStack itemStack = event.getItem();
        if (!event.hasItem() || itemStack == null) {
            return false;
        }
        List<String> list = settings.getStringList("action");
        if (!list.contains(event.getAction().name())) {
            return false;
        }
        String name = settings.getString("name");
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
        String lore = settings.getString("lore");
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
        return true;
    }

    @Override
    public void setValues(final PlayerInteractEvent event, final Map<String, Object> data) {
        data.put("api-item-type", Objects.requireNonNull(event.getItem()).getType().name());
    }

    @Override
    public LivingEntity getCaster(final PlayerInteractEvent event) {
        return event.getPlayer();
    }

    @Override
    public LivingEntity getTarget(final PlayerInteractEvent event, final Settings settings) {
        return event.getPlayer();
    }
}

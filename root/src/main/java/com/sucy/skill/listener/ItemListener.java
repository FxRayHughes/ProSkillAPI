/**
 * SkillAPI
 * com.sucy.skill.listener.ItemListener
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.listener;

import com.google.common.collect.ImmutableSet;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerClassChangeEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.data.PlayerEquipsRead;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * Listener that handles weapon item lore requirements
 */
public class ItemListener extends SkillAPIListener {
    @Override
    public void init() {
        MainListener.registerJoin(this::onJoin);
    }


    /**
     * Removes weapon bonuses when dropped
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld()))
            PlayerEquipsRead.update(SkillAPI.getPlayerData(event.getPlayer()));
    }

    /**
     * Updates player equips when an item breaks
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(PlayerItemBreakEvent event) {
        if (SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld()))
            SkillAPI.schedule(new UpdateTask(event.getPlayer(), false), 1);
    }

    /**
     * Updates equipment data on join
     */
    public void onJoin(final Player player) {
        if (SkillAPI.getSettings().isWorldEnabled(player.getWorld()))
            PlayerEquipsRead.update(SkillAPI.getPlayerData(player));
    }

    @EventHandler
    public void onProfess(PlayerClassChangeEvent event) {
        final Player player = event.getPlayerData().getPlayer();
        if (SkillAPI.getSettings().isWorldEnabled(player.getWorld()))
            PlayerEquipsRead.update(event.getPlayerData());
    }

    /**
     * Updates weapon on pickup
     * Clear attribute buff data on quit
     *
     * @param event event details
     */
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld()))
            SkillAPI.schedule(new UpdateTask(event.getPlayer(), false), 1);
    }

    /**
     * Update equips on world change into an active world
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorld(PlayerChangedWorldEvent event) {
        if (!SkillAPI.getSettings().isWorldEnabled(event.getFrom())
                && SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld())) {
            PlayerEquipsRead.update(SkillAPI.getPlayerData(event.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        if (SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld()))
            SkillAPI.schedule(new UpdateTask(event.getPlayer(), true), 1);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld()))
            SkillAPI.schedule(new UpdateTask((Player) event.getPlayer(), false), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld())
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.getPlayer().getInventory().getItemInMainHand();
            if (ARMOR_TYPES.contains(event.getPlayer().getItemInHand().getType())) {
                SkillAPI.schedule(new UpdateTask(event.getPlayer(), false), 1);
            }
        }
    }

    /**
     * Cancels left clicks on disabled items
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!SkillAPI.getSettings().isWorldEnabled(event.getEntity().getWorld())) {
            return;
        }

//        if (event.getDamager() instanceof Player) {
//            Player player = (Player) event.getDamager();
//            if (!SkillAPI.getPlayerData(player).getEquips().canHit()) {
//                SkillAPI.getLanguage().sendMessage(ErrorNodes.CANNOT_USE, player, FilterType.COLOR);
//                event.setCancelled(true);
//            }
//        }
    }

    /**
     * Cancels firing a bow with a disabled weapon
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(EntityShootBowEvent event) {
        if (!SkillAPI.getSettings().isWorldEnabled(event.getEntity().getWorld())) {
            return;
        }

        if (event.getEntity() instanceof Player) {
//            final PlayerEquips equips = SkillAPI.getPlayerData((Player) event.getEntity()).getEquips();
//            if (isMainhand(event.getBow(), event.getEntity())) {
//                if (!equips.canHit()) {
//                    SkillAPI.getLanguage().sendMessage(ErrorNodes.CANNOT_USE, event.getEntity(), FilterType.COLOR);
//                    event.setCancelled(true);
//                }
//            } else if (!equips.canBlock()) {
//                SkillAPI.getLanguage().sendMessage(ErrorNodes.CANNOT_USE, event.getEntity(), FilterType.COLOR);
//                event.setCancelled(true);
//            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!SkillAPI.getSettings().isWorldEnabled(event.getEntity().getWorld())) {
            return;
        }
//        ProjectileSource shooter = event.getEntity().getShooter();
//        if (shooter instanceof Player) {
//            Player player = (Player) shooter;
//            final PlayerEquips equips = SkillAPI.getPlayerData(player).getEquips();
//            if (!equips.canHit()) {
//                SkillAPI.getLanguage().sendMessage(ErrorNodes.CANNOT_USE, player, FilterType.COLOR);
//                event.setCancelled(true);
//            }
//        }
    }

    public static final Set<Material> ARMOR_TYPES = getArmorMaterials();

    private static Set<Material> getArmorMaterials() {
        final Set<String> armorSuffixes = ImmutableSet.of("BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET");
        final ImmutableSet.Builder<Material> builder = ImmutableSet.builder();
        for (Material material : Material.values()) {
            final int index = material.name().lastIndexOf('_') + 1;
            final String suffix = material.name().substring(index);
            if (armorSuffixes.contains(suffix)) {
                builder.add(material);
            }
        }
        return builder.build();
    }

    /**
     * Handles updating equipped armor
     */
    private static class UpdateTask extends BukkitRunnable {
        private final Player player;

        private final boolean weaponOnly;

        /**
         * @param player player reference
         */
        UpdateTask(Player player, boolean weaponOnly) {
            this.player = player;
            this.weaponOnly = weaponOnly;
        }

        /**
         * Applies the update
         */
        @Override
        public void run() {
            PlayerData data = SkillAPI.getPlayerData(player);
            if (data == null) {
                return;
            }
            if (weaponOnly) {
                PlayerEquipsRead.updateHand(data, false);
            } else {
                PlayerEquipsRead.update(data);
            }
        }
    }
}

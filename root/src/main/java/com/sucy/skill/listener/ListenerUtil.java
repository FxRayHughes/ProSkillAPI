package com.sucy.skill.listener;

import com.rit.sucy.reflect.Reflection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Helper class for listeners
 */
public class ListenerUtil {
    /**
     * Retrieves a damager from an entity damage event which will get the
     * shooter of projectiles if it was a projectile hitting them or
     * converts the Entity damager to a LivingEntity if applicable.
     *
     * @param event event to grab the damager from
     *
     * @return LivingEntity damager of the event or null if not found
     */
    public static LivingEntity getDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity) {
            return (LivingEntity) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof LivingEntity) {
                return (LivingEntity) projectile.getShooter();
            }
        }
        return null;
    }

    public static Inventory getClickedInventory(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        InventoryView view = event.getView();
        if (slot < 0) {
            return null;
        } else if ((view.getTopInventory() != null) && (slot < view.getTopInventory().getSize())) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }

    /**
     * Gets a simple name of the entity
     *
     * @param entity entity to get the name of
     *
     * @return simple name of the entity
     */
    public static String getName(Entity entity) {
        String name = entity.getClass().getSimpleName().toLowerCase().replace("craft", "");
        if (entity.getType().name().equals("SKELETON")) {
            try {
                Object type = Reflection.getMethod(entity, "getSkeletonType").invoke(entity);
                if (Reflection.getMethod(type, "name").invoke(type).equals("WITHER")) {
                    name = "wither" + name;
                }
            } catch (Exception ex) { /* Wither skeletons don't exist */ }
        } else if (entity.getType().name().equals("GUARDIAN")) {
            try {
                if ((Boolean) Reflection.getMethod(entity, "isElder").invoke(entity)) {
                    name = "elder" + name;
                }
            } catch (Exception ex) { /* Shouldn't error out */ }
        }
        return name;
    }
}

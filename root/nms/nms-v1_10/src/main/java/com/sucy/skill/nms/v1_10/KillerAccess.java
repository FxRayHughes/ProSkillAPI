package com.sucy.skill.nms.v1_10;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Writes the internal "who killed this entity" state so vanilla drops and
 * statistics credit the player that a skill actually damaged.
 *
 * <p>The field names {@code killer} and {@code lastDamageByPlayerTime} are
 * stable across the whole pre-flattening range, which is why one implementation
 * serves 1.8 through 1.12.</p>
 */
public class KillerAccess {
    protected Method handle;
    protected Field killer;
    protected Field damageTime;

    public KillerAccess() {
        try {
            Class<?> living = LegacyReflection.nmsClass("EntityLiving");
            handle = LegacyReflection.craftClass("entity.CraftEntity").getDeclaredMethod("getHandle");
            killer = living.getDeclaredField("killer");
            killer.setAccessible(true);
            damageTime = living.getDeclaredField("lastDamageByPlayerTime");
            damageTime.setAccessible(true);
        } catch (Exception ignored) {
            // Handled by returning false from mark; the caller uses metadata.
            handle = null;
        }
    }

    /**
     * @param entity damaged entity
     * @param player player to credit
     * @return true when the native state was updated
     */
    public boolean mark(LivingEntity entity, Player player) {
        if (handle == null) {
            return false;
        }
        try {
            Object hit = handle.invoke(entity);
            Object source = handle.invoke(player);
            killer.set(hit, source);
            damageTime.set(hit, 100);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}

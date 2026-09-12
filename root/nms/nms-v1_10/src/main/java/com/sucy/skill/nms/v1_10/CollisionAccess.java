package com.sucy.skill.nms.v1_10;

import com.google.common.base.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Exact projectile collision using the server's own axis-aligned bounding box
 * query, which is more accurate than a radius check around entity origins.
 *
 * <p>The predicate parameter is the one moving part: 1.8 through the early 1.12
 * builds take a Guava {@code Predicate}, later builds take the JDK one. Both
 * overloads are probed so a single class covers the whole legacy range.</p>
 */
public class CollisionAccess {
    protected Constructor<?> aabbConstructor;
    protected Method getEntities;
    protected Method getEntitiesGuava;
    protected Method getBukkitEntity;
    protected Method getHandle;

    public CollisionAccess() {
        try {
            Class<?> aabbClass = LegacyReflection.nmsClass("AxisAlignedBB");
            Class<?> entityClass = LegacyReflection.nmsClass("Entity");
            aabbConstructor = aabbClass.getConstructor(
                    double.class, double.class, double.class,
                    double.class, double.class, double.class);
            getBukkitEntity = entityClass.getDeclaredMethod("getBukkitEntity");
            getHandle = LegacyReflection.craftClass("CraftWorld").getDeclaredMethod("getHandle");
            Class<?> worldClass = LegacyReflection.nmsClass("World");
            try {
                getEntities = worldClass.getDeclaredMethod(
                        "getEntities", entityClass, aabbClass, java.util.function.Predicate.class);
            } catch (Exception ignored) {
                getEntitiesGuava = worldClass.getDeclaredMethod(
                        "getEntities", entityClass, aabbClass, Predicate.class);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().info(
                    "[SkillAPI] Unable to use reflection for accurate collision - "
                            + "falling back to a chunk scan");
            getHandle = null;
        }
    }

    /**
     * @param location collision centre
     * @param radius collision radius
     * @return living entities in the box, or null when reflection is unusable
     */
    public List<LivingEntity> get(Location location, double radius) {
        if (getHandle == null) {
            return null;
        }
        try {
            List<LivingEntity> result = new ArrayList<LivingEntity>(1);
            Object nmsWorld = getHandle.invoke(location.getWorld());
            Object list = (getEntities == null ? getEntitiesGuava : getEntities)
                    .invoke(nmsWorld, null, box(location, radius), predicate());
            for (Object item : (List<?>) list) {
                Object entity = getBukkitEntity.invoke(item);
                if (entity instanceof LivingEntity) {
                    result.add((LivingEntity) entity);
                }
            }
            return result;
        } catch (Exception ignored) {
            return null;
        }
    }

    protected Object box(Location loc, double radius) throws Exception {
        return aabbConstructor.newInstance(
                loc.getX() - radius, loc.getY() - radius, loc.getZ() - radius,
                loc.getX() + radius, loc.getY() + radius, loc.getZ() + radius);
    }

    protected Object predicate() {
        if (getEntities == null) {
            return (Predicate<Object>) this::isLivingEntity;
        }
        return (java.util.function.Predicate<Object>) this::isLivingEntity;
    }

    protected boolean isLivingEntity(Object value) {
        try {
            return getBukkitEntity.invoke(value) instanceof LivingEntity;
        } catch (Exception ignored) {
            return false;
        }
    }
}

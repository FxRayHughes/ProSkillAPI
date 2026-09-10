/**
 * SkillAPI
 * com.sucy.skill.dynamic.TempEntityHandler
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.dynamic;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.api.particle.target.EffectTarget;
import com.sucy.skill.api.particle.target.EntityTarget;
import com.sucy.skill.api.particle.target.FixedTarget;
import com.sucy.skill.api.util.Nearby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Behaviour behind the {@link TempEntity} proxy.
 *
 * <p>A location can answer questions about where it is and what world it is in,
 * and it can be moved. It cannot answer anything about health, inventory,
 * potion effects, AI, or packets. This handler implements the first group by
 * name and returns the empty value for the second, which is what the previous
 * hand-written stubs did - only without needing one stub per API method.</p>
 *
 * <p>Dispatch is by method name and parameter count. Names are matched rather
 * than {@code Method} objects because the declaring interface and its exact
 * signatures differ between server versions; a name plus an arity is the part
 * that has stayed stable.</p>
 */
final class TempEntityHandler implements InvocationHandler {
    /** Empty values for the primitive return types, keyed by return class. */
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS =
            new ConcurrentHashMap<Class<?>, Object>();

    static {
        PRIMITIVE_DEFAULTS.put(boolean.class, Boolean.FALSE);
        PRIMITIVE_DEFAULTS.put(byte.class, (byte) 0);
        PRIMITIVE_DEFAULTS.put(short.class, (short) 0);
        PRIMITIVE_DEFAULTS.put(int.class, 0);
        PRIMITIVE_DEFAULTS.put(long.class, 0L);
        PRIMITIVE_DEFAULTS.put(float.class, 0f);
        PRIMITIVE_DEFAULTS.put(double.class, 0d);
        PRIMITIVE_DEFAULTS.put(char.class, '\0');
    }

    private final UUID id = UUID.randomUUID();

    /** Reassigned by the teleport calls, so the dummy can follow a target. */
    private volatile EffectTarget target;

    TempEntityHandler(EffectTarget target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object[] params = args == null ? new Object[0] : args;
        String name = method.getName();

        switch (name) {
            case "getEffectTarget":
                return target;

            // --- identity -------------------------------------------------
            case "getUniqueId":
                return id;
            case "getEntityId":
                return id.hashCode();
            case "getName":
            case "getCustomName":
            case "getScoreboardEntryName":
                return "Location";
            case "getType":
                // A concrete type is required by callers that switch on it;
                // the value itself is arbitrary and never spawned.
                return EntityType.CHICKEN;
            case "getServer":
                return Bukkit.getServer();

            // --- position -------------------------------------------------
            case "getLocation":
                return params.length == 0 ? location() : copyInto((Location) params[0]);
            case "getEyeLocation":
                return location().add(0, 1, 0);
            case "getEyeHeight":
                return 0.2d;
            case "getWorld":
                return target.getLocation().getWorld();
            case "getX":
                return target.getLocation().getX();
            case "getY":
                return target.getLocation().getY();
            case "getZ":
                return target.getLocation().getZ();
            case "getYaw":
                return target.getLocation().getYaw();
            case "getPitch":
                return target.getLocation().getPitch();
            case "getVelocity":
                return new Vector(0, 0, 0);

            // --- movement -------------------------------------------------
            case "teleport":
            case "teleportAsync":
                return teleport(method, params);

            // --- surroundings ---------------------------------------------
            case "getNearbyEntities":
                return nearbyEntities(params);

            // --- health ---------------------------------------------------
            // A dummy must read as alive so targeting filters do not drop it.
            case "getHealth":
            case "getMaxHealth":
                return 1d;
            case "isValid":
                return Boolean.TRUE;
            case "isDead":
                return Boolean.FALSE;

            // --- collections ----------------------------------------------
            // Empty rather than null: callers iterate these directly.
            case "getActivePotionEffects":
                return ImmutableList.of();
            case "getPassengers":
            case "getLineOfSight":
            case "getLastTwoTargetBlocks":
                return ImmutableList.of();
            case "getCollidableExemptions":
                return Collections.emptySet();
            case "getEffectivePermissions":
                return Collections.emptySet();

            // --- Object contract ------------------------------------------
            // Proxies route these here too, and identity semantics are what
            // the previous class had.
            case "equals":
                return proxy == params[0];
            case "hashCode":
                return id.hashCode();
            case "toString":
                return "TempEntity{" + target.getLocation() + "}";

            default:
                return emptyValue(method.getReturnType());
        }
    }

    private Location location() {
        return target.getLocation().clone();
    }

    private Location copyInto(Location destination) {
        if (destination == null) {
            return null;
        }
        Location loc = target.getLocation();
        destination.setWorld(loc.getWorld());
        destination.setX(loc.getX());
        destination.setY(loc.getY());
        destination.setZ(loc.getZ());
        destination.setYaw(loc.getYaw());
        destination.setPitch(loc.getPitch());
        return destination;
    }

    /**
     * Retargets the dummy. Every teleport overload carries the destination
     * first, so only that argument is inspected and the rest - teleport cause,
     * flags - is irrelevant to a dummy.
     */
    private Object teleport(Method method, Object[] params) {
        if (params.length > 0) {
            if (params[0] instanceof Location) {
                target = new FixedTarget((Location) params[0]);
            } else if (params[0] instanceof Entity) {
                target = new EntityTarget((Entity) params[0]);
            }
        }
        // teleportAsync returns a future; teleport returns a boolean.
        if (method.getReturnType() == boolean.class) {
            return Boolean.TRUE;
        }
        return emptyValue(method.getReturnType());
    }

    private List<Entity> nearbyEntities(Object[] params) {
        double radius = params.length > 0 && params[0] instanceof Number
                ? ((Number) params[0]).doubleValue()
                : 0;
        return Nearby.getNearby(target.getLocation(), radius);
    }

    private static Object emptyValue(Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        return returnType.isPrimitive() ? PRIMITIVE_DEFAULTS.get(returnType) : null;
    }
}

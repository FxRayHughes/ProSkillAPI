/**
 * SkillAPI
 * com.sucy.skill.nms.NmsCapabilities
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.nms;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime capability probes.
 *
 * <p>The plugin compiles against one recent Paper API but has to run on cores
 * where individual methods were added, deprecated, or removed. Compile-time
 * version guards cannot express that, so every version-sensitive call site asks
 * here first and picks an implementation that actually exists on the running
 * core. Probe results are cached because reflection lookups are comparatively
 * expensive and the answer cannot change inside one server process.</p>
 */
public final class NmsCapabilities {
    private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<String, Boolean>();

    private NmsCapabilities() {
        // Utility class: probes are process-wide and stateless.
    }

    /**
     * @param name fully qualified class name
     * @return true when the class can be loaded by the plugin class loader
     */
    public static boolean hasClass(String name) {
        Boolean cached = CACHE.get("c:" + name);
        if (cached != null) {
            return cached;
        }
        boolean present;
        try {
            Class.forName(name, false, NmsCapabilities.class.getClassLoader());
            present = true;
        } catch (Throwable ignored) {
            present = false;
        }
        CACHE.put("c:" + name, present);
        return present;
    }

    /**
     * @param owner declaring type
     * @param name method name
     * @param parameters parameter types
     * @return true when the method exists on the running core
     */
    public static boolean hasMethod(Class<?> owner, String name, Class<?>... parameters) {
        String key = "m:" + owner.getName() + "#" + name + "/" + parameters.length;
        Boolean cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        boolean present = findMethod(owner, name, parameters) != null;
        CACHE.put(key, present);
        return present;
    }

    /**
     * Resolves a method without throwing when it is absent.
     *
     * @param owner declaring type
     * @param name method name
     * @param parameters parameter types
     * @return accessible method, or null when the core does not declare it
     */
    public static Method findMethod(Class<?> owner, String name, Class<?>... parameters) {
        Class<?> type = owner;
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }
        try {
            return owner.getMethod(name, parameters);
        } catch (Throwable ignored) {
            // Interface default methods on relocated cores may still be absent.
            return null;
        }
    }

    /**
     * @param type enum class
     * @param constant constant name
     * @return true when the enum declares the constant on this core
     */
    public static boolean hasEnumConstant(Class<?> type, String constant) {
        String key = "e:" + type.getName() + "#" + constant;
        Boolean cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        boolean present = false;
        Object[] constants = type.getEnumConstants();
        if (constants != null) {
            for (Object value : constants) {
                if (((Enum<?>) value).name().equals(constant)) {
                    present = true;
                    break;
                }
            }
        }
        CACHE.put(key, present);
        return present;
    }
}

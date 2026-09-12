package com.sucy.skill.nms.v1_12;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflective access to the versioned {@code net.minecraft.server.vX_Y_RZ} and
 * {@code org.bukkit.craftbukkit.vX_Y_RZ} packages.
 *
 * <p>Every pre-flattening generation shares this lookup, so it lives in the
 * oldest supported module and is inherited by the later legacy modules through
 * their Gradle dependency chain.</p>
 */
public final class LegacyReflection {
    private LegacyReflection() {
        // Utility class: the server package is fixed for the process lifetime.
    }

    /**
     * @return the {@code v1_8_R3}-style package suffix of the running core
     */
    public static String serverVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static String nmsPackage() {
        return "net.minecraft.server." + serverVersion() + ".";
    }

    public static String craftPackage() {
        return "org.bukkit.craftbukkit." + serverVersion() + ".";
    }

    public static Class<?> nmsClass(String name) throws ClassNotFoundException {
        return Class.forName(nmsPackage() + name);
    }

    public static Class<?> craftClass(String name) throws ClassNotFoundException {
        return Class.forName(craftPackage() + name);
    }

    /**
     * @param source instance to read from
     * @param field public field name
     * @return field value
     * @throws Exception when the field does not exist on this core
     */
    public static Object getValue(Object source, String field) throws Exception {
        Field value = source.getClass().getField(field);
        return value.get(source);
    }

    /**
     * Walks the type hierarchy because the declaring class differs between
     * generations even when the method name does not.
     *
     * @param source instance whose type is searched
     * @param name method name
     * @param arg single parameter type
     * @return the method, or null when absent
     */
    public static Method getMethod(Object source, String name, Class<?> arg) {
        Class<?> type = source.getClass();
        while (type != null) {
            try {
                return type.getDeclaredMethod(name, arg);
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }
}

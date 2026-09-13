/**
 * SkillAPI
 * com.sucy.skill.compat.bukkit.EnumCompat
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.compat.bukkit;

import org.bukkit.NamespacedKey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 按名字解析 Bukkit 常量，不与 enum / interface 形态绑定。
 *
 * <p>Bukkit 正在把一批 enum 改成由注册表驱动的 interface：Sound 从 1.21.3 起、
 * Attribute 从 1.21.11 起。同一时期不同核心的进度还不一致（Spigot 1.21.11 的
 * Attribute 已是 interface，同期 Paper 仍是 enum）。</p>
 *
 * <p>问题出在字节码层面：{@code Sound.valueOf(x)} 在编译期就被固定成
 * Methodref（enum）或 InterfaceMethodref（interface）。用一方的 API 编译、
 * 在另一方运行时，方法链接阶段直接抛 {@link IncompatibleClassChangeError}，
 * 而它发生在进入方法体之前，调用处的 try/catch 拦不住。</p>
 *
 * <p>反射不区分这两种引用类型，因此是唯一能同时适配两种形态的写法。</p>
 */
public final class EnumCompat {

    private EnumCompat() {
    }

    /**
     * 取指定常量容器里名为 {@code name} 的常量。
     *
     * @param type     常量容器，如 {@code Sound.class}
     * @param name     常量名，大小写与下划线需与服务端一致
     * @param registry 可选的注册表兜底，用于没有 valueOf 的纯注册表实现；可为 null
     * @param <T>      常量类型
     * @return 对应常量；此服务端没有该名字时返回 null
     */
    public static <T> T valueOf(Class<T> type, String name, Object registry) {
        if (type == null || name == null || name.isEmpty()) {
            return null;
        }
        try {
            Method valueOf = type.getMethod("valueOf", String.class);
            return type.cast(valueOf.invoke(null, name));
        } catch (InvocationTargetException ex) {
            // valueOf 对未知名字抛 IllegalArgumentException，属正常的"此版本没有这个常量"
            return null;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return fromRegistry(type, name, registry);
        }
    }

    /**
     * 注册表兜底：键为小写、minecraft 命名空间。
     *
     * <p>注册表要等服务器初始化后才可用，所以只在 valueOf 缺失时才走这条路。</p>
     */
    private static <T> T fromRegistry(Class<T> type, String name, Object registry) {
        if (registry == null) {
            return null;
        }
        try {
            Method get = registry.getClass().getMethod("get", NamespacedKey.class);
            Object value = get.invoke(registry, NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
            return type.isInstance(value) ? type.cast(value) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}

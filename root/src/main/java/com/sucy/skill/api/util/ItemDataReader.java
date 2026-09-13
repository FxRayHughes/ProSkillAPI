package com.sucy.skill.api.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Method;

/**
 * 统一读取物品持久化数据的兼容层。
 *
 * <p>使用反射调用 PersistentDataContainer，避免在 1.13 及更低版本加载
 * SkillAPI 时直接链接不存在的 Bukkit 类；Lore 读取逻辑仍由调用方负责回退。</p>
 */
public final class ItemDataReader {

    private ItemDataReader() {
    }

    /** 读取指定命名空间键的原始值，找不到或版本不支持时返回 null。 */
    public static Object get(ItemStack item, String namespace, String key) {
        if (item == null || namespace == null || key == null) return null;
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            Method getContainer = meta.getClass().getMethod("getPersistentDataContainer");
            Object container = getContainer.invoke(meta);
            Class<?> namespacedKey = Class.forName("org.bukkit.NamespacedKey");
            Object nk = namespacedKey.getConstructor(String.class, String.class)
                    .newInstance(namespace.toLowerCase(), key.toLowerCase());
            Method has = container.getClass().getMethod("has", namespacedKey, Class.forName("org.bukkit.persistence.PersistentDataType"));
            Class<?> type = Class.forName("org.bukkit.persistence.PersistentDataType");
            Object stringType = type.getField("STRING").get(null);
            if (Boolean.TRUE.equals(has.invoke(container, nk, stringType))) {
                return container.getClass().getMethod("get", namespacedKey, type).invoke(container, nk, stringType);
            }
            Object intType = type.getField("INTEGER").get(null);
            if (Boolean.TRUE.equals(has.invoke(container, nk, intType))) {
                return container.getClass().getMethod("get", namespacedKey, type).invoke(container, nk, intType);
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // 旧 Bukkit 没有 PDC，读取层必须静默回退到 Lore 或其他适配器。
        }
        return null;
    }

    public static String getString(ItemStack item, String namespace, String key) {
        Object value = get(item, namespace, key);
        return value == null ? null : String.valueOf(value);
    }

    public static Integer getInt(ItemStack item, String namespace, String key) {
        Object value = get(item, namespace, key);
        if (value instanceof Number) return ((Number) value).intValue();
        try { return value == null ? null : Integer.valueOf(String.valueOf(value)); }
        catch (NumberFormatException ignored) { return null; }
    }

    /** 读取自定义 NBT Compound 下的字符串列表，用于并行承载多个属性或技能。 */
    public static List<String> getStringList(ItemStack item, String compound, String key) {
        if (item == null || compound == null || key == null) return new ArrayList<>();
        try {
            ReadWriteNBT root = new NBTItem(item);
            ReadWriteNBT node = root.getCompound(compound);
            if (node == null) return new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (String value : node.getStringList(key)) values.add(value);
            return values;
        } catch (RuntimeException ignored) {
            // 第三方物品或旧版本 NBT 结构异常时，继续使用 Lore 数据。
            return new ArrayList<>();
        }
    }
}

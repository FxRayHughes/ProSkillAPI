/**
 * SkillAPI
 * com.sucy.skill.serialization.legacy.LegacyNbtItemSerializationService
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization.legacy;

import com.sucy.skill.serialization.ItemSerializationService;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * Read-only adapter for the original NMS NBT base32 inventory format. New data
 * is written by the Gson module, but this module keeps old player files
 * recoverable on legacy servers where that format may already be persisted.
 */
public final class LegacyNbtItemSerializationService implements ItemSerializationService {
    private final ItemNbtAccess access = new ItemNbtAccess();

    @Override
    public String serialize(ItemStack[] items) {
        return null;
    }

    @Override
    public ItemStack[] deserialize(String data) {
        if (data == null || data.startsWith("cbor-v1:") || data.startsWith("bukkit:") || data.indexOf(';') >= 0) {
            return null;
        }
        return access.deserialize(data);
    }

    private static String nmsPackage() {
        return "net.minecraft.server." + serverVersion() + ".";
    }

    private static String craftPackage() {
        return "org.bukkit.craftbukkit." + serverVersion() + ".";
    }

    private static String serverVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static Class<?> nmsClass(String name) throws ClassNotFoundException {
        return Class.forName(nmsPackage() + name);
    }

    private static Class<?> craftClass(String name) throws ClassNotFoundException {
        return Class.forName(craftPackage() + name);
    }

    private static final class ItemNbtAccess {
        private Constructor<?> craftItemNMSConstructor;
        private Constructor<?> nmsItemConstructor;
        private Method nbtTagListSize;
        private Method nbtTagListGet;
        private Method nbtCompressedStreamToolsRead;
        private Method nbtTagCompoundGetList;
        private Method nbtTagCompoundIsEmpty;

        private ItemNbtAccess() {
            try {
                Class<?> craftItemStack = craftClass("inventory.CraftItemStack");
                Class<?> nmsItemStack = nmsClass("ItemStack");
                craftItemNMSConstructor = craftItemStack.getDeclaredConstructor(nmsItemStack);
                craftItemNMSConstructor.setAccessible(true);

                Class<?> nbtTagCompound = nmsClass("NBTTagCompound");
                Class<?> nbtTagList = nmsClass("NBTTagList");
                Class<?> nbtCompressedStreamTools = nmsClass("NBTCompressedStreamTools");
                nmsItemConstructor = nmsItemStack.getDeclaredConstructor(nbtTagCompound);
                nmsItemConstructor.setAccessible(true);
                nbtTagCompoundGetList = nbtTagCompound.getDeclaredMethod("getList", String.class, int.class);
                nbtTagCompoundIsEmpty = nbtTagCompound.getDeclaredMethod("isEmpty");
                nbtTagListSize = nbtTagList.getDeclaredMethod("size");
                nbtTagListGet = nbtTagList.getDeclaredMethod("get", int.class);
                nbtCompressedStreamToolsRead = nbtCompressedStreamTools.getDeclaredMethod("a", DataInputStream.class);
            } catch (Exception ignored) {
                // Unsupported server internals simply make the legacy reader unavailable.
            }
        }

        private ItemStack[] deserialize(String data) {
            if (nbtCompressedStreamToolsRead == null) {
                return null;
            }
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                Object wrapper = nbtCompressedStreamToolsRead.invoke(null, dataInputStream);
                Object itemList = nbtTagCompoundGetList.invoke(wrapper, "i", 10);
                ItemStack[] items = new ItemStack[(Integer) nbtTagListSize.invoke(itemList)];
                for (int i = 0; i < items.length; i++) {
                    Object inputObject = nbtTagListGet.invoke(itemList, i);
                    if (!(Boolean) nbtTagCompoundIsEmpty.invoke(inputObject)) {
                        items[i] = (ItemStack) craftItemNMSConstructor.newInstance(nmsItemConstructor.newInstance(inputObject));
                    }
                }
                return items;
            } catch (Exception ex) {
                return null;
            }
        }
    }
}

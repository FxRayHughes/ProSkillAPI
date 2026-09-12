package com.sucy.skill.nms.v1_8;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Action bar delivery through {@code PacketPlayOutChat}.
 *
 * <p>1.8 through 1.11 select the action bar slot with a raw {@code byte}
 * position argument. 1.12 replaced that argument with the {@code ChatMessageType}
 * enum, which is why {@code V1_12Bridge} substitutes a subclass rather than
 * branching inside this one.</p>
 */
public class ActionBarAccess {
    protected Class<?> packet;
    protected Method getHandle;
    protected Constructor<?> constructPacket;
    protected Constructor<?> constructText;
    protected Object messageType = (byte) 2;
    protected boolean supported;

    public ActionBarAccess() {
        try {
            Class<?> chatPacket = LegacyReflection.nmsClass("PacketPlayOutChat");
            Class<?> chatBase = LegacyReflection.nmsClass("IChatBaseComponent");
            Class<?> chatText = LegacyReflection.nmsClass("ChatComponentText");
            packet = LegacyReflection.nmsClass("Packet");
            constructPacket = resolvePacketConstructor(chatPacket, chatBase);
            constructText = chatText.getConstructor(String.class);
            getHandle = LegacyReflection.craftClass("entity.CraftPlayer").getDeclaredMethod("getHandle");
            supported = true;
        } catch (Exception ignored) {
            supported = false;
        }
    }

    /**
     * @param chatPacket the {@code PacketPlayOutChat} class
     * @param chatBase the {@code IChatBaseComponent} class
     * @return constructor taking the component and this generation's slot argument
     */
    protected Constructor<?> resolvePacketConstructor(Class<?> chatPacket, Class<?> chatBase)
            throws Exception {
        return chatPacket.getConstructor(chatBase, byte.class);
    }

    public boolean isSupported() {
        return supported;
    }

    /**
     * @param player recipient
     * @param message formatted legacy text
     * @return true when the packet was sent
     */
    public boolean send(Player player, String message) {
        if (!supported) {
            return false;
        }
        try {
            Object text = constructText.newInstance(message);
            Object data = constructPacket.newInstance(text, messageType);
            Object handle = getHandle.invoke(player);
            Object connection = LegacyReflection.getValue(handle, "playerConnection");
            Method send = LegacyReflection.getMethod(connection, "sendPacket", packet);
            if (send == null) {
                return false;
            }
            send.invoke(connection, data);
            return true;
        } catch (Exception ignored) {
            // A failure here is structural, not transient; stop retrying.
            supported = false;
            return false;
        }
    }
}

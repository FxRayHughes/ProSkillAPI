package com.sucy.skill.nms.v1_11;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Title delivery through {@code PacketPlayOutTitle}, which is the only option
 * before Bukkit added {@code Player#sendTitle} with timings in 1.11.
 */
public class TitleAccess {
    protected Constructor<?> timesConstructor;
    protected Constructor<?> contentConstructor;
    protected Class<?> packetTitle;
    protected Method serialize;
    protected Object timesType;
    protected Object titleType;
    protected Object subtitleType;
    protected boolean failed;

    /**
     * @param player recipient
     * @param title title text, nullable
     * @param subtitle subtitle text, nullable
     * @param fadeIn fade-in ticks
     * @param duration stay ticks
     * @param fadeOut fade-out ticks
     */
    public void send(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        if (failed) {
            return;
        }
        try {
            if (packetTitle == null) {
                load();
            }
            if (title != null) {
                sendPart(player, title, titleType, fadeIn, duration, fadeOut);
            }
            if (subtitle != null) {
                sendPart(player, subtitle, subtitleType, fadeIn, duration, fadeOut);
            }
        } catch (Exception ignored) {
            // Titles are cosmetic; a broken core must not interrupt a skill.
            failed = true;
        }
    }

    protected void load() throws Exception {
        packetTitle = LegacyReflection.nmsClass("PacketPlayOutTitle");
        Class<?> chatBaseComponent = LegacyReflection.nmsClass("IChatBaseComponent");
        serialize = chatBaseComponent.getDeclaredClasses()[0].getDeclaredMethod("a", String.class);
        Class<?> titleSerializer = packetTitle.getDeclaredClasses()[0];
        timesType = titleSerializer.getField("TIMES").get(null);
        titleType = titleSerializer.getField("TITLE").get(null);
        subtitleType = titleSerializer.getField("SUBTITLE").get(null);
        timesConstructor = packetTitle.getConstructor(
                titleSerializer, chatBaseComponent, int.class, int.class, int.class);
        contentConstructor = packetTitle.getConstructor(titleSerializer, chatBaseComponent);
    }

    protected void sendPart(
            Player player, String text, Object type, int fadeIn, int duration, int fadeOut)
            throws Exception {
        Object chatText = serialize.invoke(null, "{\"text\":\"" + escape(text) + "\"}");
        sendPacket(player, timesConstructor.newInstance(timesType, chatText, fadeIn, duration, fadeOut));
        sendPacket(player, contentConstructor.newInstance(type, chatText));
    }

    /**
     * The title payload is hand-built JSON, so quotes and backslashes coming
     * from a skill's configured message would otherwise produce a malformed
     * component and drop the title silently.
     */
    protected static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    protected void sendPacket(Player player, Object packet) throws Exception {
        Object handle = LegacyReflection.craftClass("entity.CraftPlayer")
                .getDeclaredMethod("getHandle").invoke(player);
        Object connection = LegacyReflection.getValue(handle, "playerConnection");
        Method send = LegacyReflection.getMethod(
                connection, "sendPacket", LegacyReflection.nmsClass("Packet"));
        if (send == null) {
            return;
        }
        send.invoke(connection, packet);
    }
}

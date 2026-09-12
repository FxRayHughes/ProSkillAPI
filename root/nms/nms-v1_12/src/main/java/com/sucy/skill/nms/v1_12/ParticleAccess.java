package com.sucy.skill.nms.v1_12;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds and sends {@code PacketPlayOutWorldParticles} directly.
 *
 * <p>1.8 replaced the string particle name with the {@code EnumParticle} type
 * and added the trailing {@code int[]} data array, which is the signature this
 * class targets. Later legacy generations kept it unchanged.</p>
 */
public class ParticleAccess {
    protected Constructor<?> packet;
    protected Method getHandle;
    protected Method sendPacket;
    protected Field connection;
    protected final Map<String, Object> particleTypes = new HashMap<String, Object>();

    public ParticleAccess() {
        try {
            String nms = LegacyReflection.nmsPackage();
            String craft = LegacyReflection.craftPackage();
            getHandle = Class.forName(craft + "entity.CraftPlayer").getMethod("getHandle");
            connection = Class.forName(nms + "EntityPlayer").getDeclaredField("playerConnection");
            sendPacket = Class.forName(nms + "PlayerConnection")
                    .getDeclaredMethod("sendPacket", Class.forName(nms + "Packet"));

            Class<?> particleEnum = Class.forName(nms + "EnumParticle");
            for (Object value : particleEnum.getEnumConstants()) {
                particleTypes.put(value.toString(), value);
            }
            packet = Class.forName(nms + "PacketPlayOutWorldParticles").getConstructor(
                    particleEnum,
                    Boolean.TYPE,
                    Float.TYPE, Float.TYPE, Float.TYPE,
                    Float.TYPE, Float.TYPE, Float.TYPE,
                    Float.TYPE,
                    Integer.TYPE,
                    int[].class);
        } catch (Exception ignored) {
            Bukkit.getLogger().warning(
                    "[SkillAPI] Failed to set up legacy particle packets on this core");
            packet = null;
        }
    }

    /**
     * @return an opaque particle packet, or null when unsupported
     */
    public Object make(
            String name,
            double x, double y, double z,
            float dx, float dy, float dz,
            float speed,
            int amount,
            Material material,
            int data) throws Exception {
        if (packet == null) {
            return null;
        }
        Object enumType = particleTypes.get(name);
        if (enumType == null) {
            return null;
        }
        return packet.newInstance(
                enumType,
                true,
                (float) x, (float) y, (float) z,
                dx, dy, dz,
                speed,
                amount,
                material == null ? new int[0] : new int[]{material.ordinal(), data});
    }

    /**
     * @param player recipient
     * @param packets packets produced by {@link #make}
     */
    public void send(Player player, Iterable<?> packets) throws Exception {
        if (connection == null || getHandle == null || sendPacket == null) {
            return;
        }
        Object network = connection.get(getHandle.invoke(player));
        for (Object value : packets) {
            if (value != null) {
                sendPacket.invoke(network, value);
            }
        }
    }
}

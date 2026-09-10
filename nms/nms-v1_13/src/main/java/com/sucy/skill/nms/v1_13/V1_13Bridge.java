package com.sucy.skill.nms.v1_13;

import com.sucy.skill.nms.KeyPressDispatcher;
import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.NmsCapabilities;
import com.sucy.skill.nms.PlayerPacketInjector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Bridge for the 1.13 generation, the first after the flattening.
 *
 * <p>This is the base of the modern chain and is deliberately NMS-free. The
 * flattening renamed or removed every internal SkillAPI used to reach for, and
 * by 1.13 Bukkit had public replacements for all of them: nearby-entity
 * queries, item flags, and the enum-based particle API. Later generations
 * extend this class and override only the calls that moved.</p>
 *
 * <p>The one behaviour that has no public replacement is killer attribution;
 * the caller falls back to Bukkit metadata, which the plugin controls.</p>
 */
public class V1_13Bridge implements NmsBridge {
    private final SpigotActionBar actionBar = new SpigotActionBar();

    @Override
    public String id() {
        return "v1_13";
    }

    @Override
    public PlayerPacketInjector createPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        // Flattened cores obfuscate packet classes on every release, so combos
        // are driven by Bukkit interact/drop events instead.
        return NoOpPacketInjector.INSTANCE;
    }

    @Override
    public boolean markKiller(LivingEntity entity, Player player) {
        return false;
    }

    @Override
    public List<LivingEntity> getColliding(Location location, double radius, LivingEntity thrower) {
        List<LivingEntity> result = new ArrayList<LivingEntity>(1);
        if (location.getWorld() == null) {
            return result;
        }
        double radiusSq = radius * radius;
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity) || entity == thrower) {
                continue;
            }
            if (location.distanceSquared(entity.getLocation()) < radiusSq) {
                result.add((LivingEntity) entity);
            }
        }
        return result;
    }

    @Override
    public boolean isActionBarSupported() {
        return actionBar.isSupported();
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        return actionBar.send(player, message);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        try {
            player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
        } catch (Throwable ignored) {
            // Titles are cosmetic; never let them interrupt a skill.
        }
    }

    @Override
    public ItemStack removeAttackDmg(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemStack result = item.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            return result;
        }
        if (result.getType().getMaxDurability() > 0) {
            meta.setUnbreakable(true);
            addFlag(meta, "HIDE_UNBREAKABLE");
        }
        addFlag(meta, "HIDE_ATTRIBUTES");
        result.setItemMeta(meta);
        return result;
    }

    @Override
    public void initParticles() {
        // Particles go through Player#spawnParticle and need no packet cache.
    }

    @Override
    public Object makeParticlePacket(
            String name,
            double x, double y, double z,
            float dx, float dy, float dz,
            float speed,
            int amount,
            Material material,
            int data) {
        return null;
    }

    @Override
    public void sendPackets(Player player, Iterable<?> packets) {
        // Nothing to send: the particle wrapper spawns directly.
    }

    /**
     * Adds an item flag only when this core still declares it. The flag enum
     * has both gained and lost constants across releases.
     */
    protected static void addFlag(ItemMeta meta, String name) {
        if (!NmsCapabilities.hasEnumConstant(ItemFlag.class, name)) {
            return;
        }
        try {
            meta.addItemFlags(ItemFlag.valueOf(name));
        } catch (Throwable ignored) {
            // Keep the item usable if the core rejects a declared flag.
        }
    }

    /**
     * Action bars through Spigot's BungeeCord-chat bridge, which is the only
     * option on 1.13 through 1.15. Resolved reflectively so that a core which
     * shipped without the BungeeCord chat classes degrades instead of failing
     * to load this class.
     */
    protected static class SpigotActionBar {
        private Method spigotAccessor;
        private Method spigotSend;
        private Object actionBarType;
        private Constructor<?> textComponent;
        private boolean resolved;
        private boolean supported;

        @SuppressWarnings("unchecked")
        private synchronized void resolve() {
            if (resolved) {
                return;
            }
            resolved = true;
            try {
                Class<?> chatType = Class.forName("net.md_5.bungee.api.ChatMessageType");
                Class<?> baseComponent = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
                Class<?> text = Class.forName("net.md_5.bungee.api.chat.TextComponent");
                spigotAccessor = Player.class.getMethod("spigot");
                spigotSend = spigotAccessor.getReturnType()
                        .getMethod("sendMessage", chatType, baseComponent);
                actionBarType = Enum.valueOf((Class<Enum>) chatType, "ACTION_BAR");
                textComponent = text.getConstructor(String.class);
                supported = true;
            } catch (Throwable ignored) {
                supported = false;
            }
        }

        protected boolean isSupported() {
            resolve();
            return supported;
        }

        protected boolean send(Player player, String message) {
            resolve();
            if (!supported) {
                return false;
            }
            try {
                Object spigot = spigotAccessor.invoke(player);
                spigotSend.invoke(spigot, actionBarType, textComponent.newInstance(message));
                return true;
            } catch (Throwable ignored) {
                supported = false;
                return false;
            }
        }
    }

    /**
     * Keeps the injector contract stable without touching Netty internals.
     */
    protected static final class NoOpPacketInjector implements PlayerPacketInjector {
        private static final NoOpPacketInjector INSTANCE = new NoOpPacketInjector();

        @Override
        public boolean isWorking() {
            return true;
        }

        @Override
        public void addPlayer(Player player) {
        }

        @Override
        public void removePlayer(Player player) {
        }
    }
}

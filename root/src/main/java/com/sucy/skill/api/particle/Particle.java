/**
 * SkillAPI
 * com.sucy.skill.api.particle.Particle
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.api.particle;

import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.nms.NmsProvider;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Simplified particle utility compared to MCCore's
 */
public class Particle {
    /**
     * Initializes the SkillAPI particle utility. Legacy packet setup is owned
     * by the NMS module; modern cores use Bukkit's public spawnParticle API.
     */
    public static void init() {
        NmsProvider.bridge().initParticles();
    }

    /**
     * Sends a list of packets to a player
     *
     * @param player  player to send to
     * @param packets packets to send
     *
     * @throws Exception
     */
    public static void send(Player player, List<Object> packets)
            throws Exception {
        if (VersionManager.isVersionAtLeast(11300)) {
            for (Object packet : packets) {
                if (packet instanceof ParticleRequest) {
                    ((ParticleRequest) packet).play(player);
                }
            }
        } else {
            NmsProvider.bridge().sendPackets(player, packets);
        }
    }

    /**
     * Sends a list of packets to a player
     *
     * @param player  player to send to
     * @param packets packets to send
     *
     * @throws Exception when reflection fails
     */
    public static void send(Player player, Object[] packets)
            throws Exception {
        if (VersionManager.isVersionAtLeast(11300)) {
            for (Object packet : packets) {
                if (packet instanceof ParticleRequest) {
                    ((ParticleRequest) packet).play(player);
                }
            }
        } else {
            ArrayList<Object> list = new ArrayList<Object>(packets.length);
            for (Object packet : packets) {
                list.add(packet);
            }
            NmsProvider.bridge().sendPackets(player, list);
        }
    }

    /**
     * Sends packets to all players within a range
     *
     * @param loc     location of the effect
     * @param packets packets from the effect
     * @param range   range to play for
     */
    public static void send(Location loc, List<Object> packets, double range)
            throws Exception {
        range *= range;
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) < range) {
                send(player, packets);
            }
        }
    }

    /**
     * Sends packets to all players within a range
     *
     * @param loc     location of the effect
     * @param packets packets from the effect
     * @param range   range to play for
     */
    public static void send(Location loc, Object[] packets, double range)
            throws Exception {
        range *= range;
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) < range) {
                send(player, packets);
            }
        }
    }

    /**
     * Make a particle packet using the given data
     *
     * @param settings particle details
     * @param loc      location to play at
     *
     * @return particle object or null if invalid
     *
     * @throws Exception
     */
    public static Object make(ParticleSettings settings, Location loc)
            throws Exception {
        return make(settings, loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Make a particle packet using the given data
     *
     * @param settings particle details
     * @param x        X coordinate
     * @param y        Y coordinate
     * @param z        Z coordinate
     *
     * @return particle object or null if invalid
     *
     * @throws Exception
     */

    public static Object make(ParticleSettings settings, double x, double y, double z) throws Exception {
        // Invalid particle settings
        if (settings == null || settings.type == null) {
            return null;
        }

        return make(
                settings.type.name(),
                x,
                y,
                z,
                settings.dx,
                settings.dy,
                settings.dz,
                settings.speed,
                settings.amount,
                settings.material,
                settings.data);
    }

    public static Object make(
            final String name,
            double x,
            double y,
            double z,
            float dx,
            float dy,
            float dz,
            float speed,
            int amount,
            Material material,
            int data) throws Exception {

        if (VersionManager.isVersionAtLeast(11300)) {
            org.bukkit.Particle particle = SpigotParticles.findParticle(name);
            if (particle == null) {
                return null;
            }
            return new ParticleRequest(particle, x, y, z, dx, dy, dz, speed, amount, material, data);
        }

        return NmsProvider.bridge().makeParticlePacket(name, x, y, z, dx, dy, dz, speed, amount, material, data);
    }

    public static boolean usesData(org.bukkit.Particle particle) {
        if (particle == null) {
            return false;
        }
        String name = particle.name();
        return isDustParticle(name) || isItemParticle(name) || isBlockParticle(name);
    }

    // Supported version for 1.13+
    public static void play(
            ArrayList<Player> players,
            org.bukkit.Particle particle,
            double x,
            double y,
            double z,
            int count,
            double dx,
            double dy,
            double dz,
            double speed,
            Material material,
            int data) {
        Object object = null;
        String name = particle.name();
        // Bukkit renamed data-carrying particles after 1.20; compare names so
        // the same source compiles against both old Bukkit and Paper 26.2 APIs.
        if (isDustParticle(name)) {
            final Color color = Color.fromRGB((int) (255 * dx), (int) (255 * dy), (int) (255 * dz));
            dx = 0;
            dy = 0;
            dz = 0;
            object = new org.bukkit.Particle.DustOptions(color, (float) speed);
        } else if (isItemParticle(name)) {
            ItemStack item = new ItemStack(material);
            if (SkillAPI.getSettings().useSkillModelData()) {
                ItemMeta meta = item.getItemMeta();
                com.sucy.skill.api.util.MaterialCompat.setCustomModelData(meta, data);
                item.setItemMeta(meta);
            } else {
                item.setData(new MaterialData(material, (byte) data));
            }
            object = item;
        } else if (isBlockParticle(name)) {
            object = material.createBlockData();
        } else if (isSpellParticle(name)) {
            // SPELL and ENTITY_EFFECT require Bukkit's typed color payload on
            // flattened servers. Resolve it reflectively so 1.12 can still
            // load this class, where Particle.Spell does not exist.
            object = createSpellData(name, dx, dy, dz, speed);
        }
        for (Player player : players) {
            if (isSpellParticle(name) && object == null) {
                // A server API without the required payload type cannot play
                // this particle safely; skip it instead of emitting a warning.
                continue;
            } else if (object == null) {
                player.spawnParticle(particle, x, y, z, count, dx, dy, dz, speed);
            } else {
                player.spawnParticle(particle, x, y, z, count, dx, dy, dz, speed, object);
            }
        }
    }

    private static boolean isDustParticle(String name) {
        return "DUST".equals(name) || "REDSTONE".equals(name);
    }

    private static boolean isItemParticle(String name) {
        return "ITEM".equals(name) || "ITEM_CRACK".equals(name);
    }

    private static boolean isBlockParticle(String name) {
        return "BLOCK".equals(name)
                || "BLOCK_CRACK".equals(name)
                || "BLOCK_DUST".equals(name)
                || "FALLING_DUST".equals(name);
    }

    private static boolean isSpellParticle(String name) {
        return "SPELL".equals(name) || "ENTITY_EFFECT".equals(name) || "EFFECT".equals(name);
    }

    private static Object createSpellData(String name, double dx, double dy, double dz, double speed) {
        try {
            Color color = Color.fromRGB(
                    clampColor(dx), clampColor(dy), clampColor(dz));
            if ("SPELL".equals(name)) {
                Class<?> type = Class.forName("org.bukkit.Particle$Spell");
                Constructor<?> constructor = type.getConstructor(Color.class, float.class);
                return constructor.newInstance(color, (float) speed);
            }
            // EFFECT and ENTITY_EFFECT use Color directly on modern Bukkit.
            return color;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static int clampColor(double value) {
        return Math.max(0, Math.min(255, (int) (value * 255.0)));
    }

    /**
     * Cached modern particle request used by skill previews. Older previews
     * cached NMS packet instances; this preserves the caching contract while
     * keeping modern servers on Bukkit's public particle API.
     */
    private static class ParticleRequest {
        private final org.bukkit.Particle particle;
        private final double x;
        private final double y;
        private final double z;
        private final float dx;
        private final float dy;
        private final float dz;
        private final float speed;
        private final int amount;
        private final Material material;
        private final int data;

        private ParticleRequest(
                org.bukkit.Particle particle,
                double x,
                double y,
                double z,
                float dx,
                float dy,
                float dz,
                float speed,
                int amount,
                Material material,
                int data) {
            this.particle = particle;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.speed = speed;
            this.amount = amount;
            this.material = material;
            this.data = data;
        }

        private void play(Player player) {
            ArrayList<Player> players = new ArrayList<Player>(1);
            players.add(player);
            Particle.play(players, particle, x, y, z, amount, dx, dy, dz, speed, material, data);
        }
    }
}

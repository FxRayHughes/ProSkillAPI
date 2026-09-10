package com.sucy.skill.nms.v1_8;

import com.sucy.skill.nms.KeyPressDispatcher;
import com.sucy.skill.nms.NmsBridge;
import com.sucy.skill.nms.PlayerPacketInjector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridge for the 1.8 NMS generation.
 *
 * <p>This is the base of the pre-flattening chain. Each later generation
 * extends the previous one and overrides only the accessors whose underlying
 * NMS contract actually changed, so a version bump is a small diff instead of a
 * copied file.</p>
 *
 * <p>Accessors are created lazily: a server that never sends a title should not
 * pay for resolving the title packet, and a core missing one accessor should
 * not lose the others.</p>
 */
public class V1_8Bridge implements NmsBridge {
    private KillerAccess killerAccess;
    private CollisionAccess collisionAccess;
    private ActionBarAccess actionBarAccess;
    private TitleAccess titleAccess;
    private DamageLoreAccess damageLoreAccess;
    private ParticleAccess particleAccess;

    @Override
    public String id() {
        return "v1_8";
    }

    @Override
    public PlayerPacketInjector createPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        return newPacketInjector(plugin, dispatcher);
    }

    @Override
    public boolean markKiller(LivingEntity entity, Player player) {
        if (killerAccess == null) {
            killerAccess = newKillerAccess();
        }
        return killerAccess.mark(entity, player);
    }

    @Override
    public List<LivingEntity> getColliding(Location location, double radius, LivingEntity thrower) {
        if (collisionAccess == null) {
            collisionAccess = newCollisionAccess();
        }
        List<LivingEntity> result = collisionAccess.get(location, radius);
        if (result == null) {
            result = getNearbyByChunks(location, radius);
        }
        result.remove(thrower);
        return result;
    }

    @Override
    public boolean isActionBarSupported() {
        if (actionBarAccess == null) {
            actionBarAccess = newActionBarAccess();
        }
        return actionBarAccess.isSupported();
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        if (actionBarAccess == null) {
            actionBarAccess = newActionBarAccess();
        }
        return actionBarAccess.send(player, message);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        if (titleAccess == null) {
            titleAccess = newTitleAccess();
        }
        titleAccess.send(player, title, subtitle, fadeIn, duration, fadeOut);
    }

    @Override
    public ItemStack removeAttackDmg(ItemStack item) {
        if (item == null) {
            return null;
        }
        if (damageLoreAccess == null) {
            damageLoreAccess = newDamageLoreAccess();
        }
        return damageLoreAccess.remove(item);
    }

    @Override
    public void initParticles() {
        if (particleAccess == null) {
            particleAccess = newParticleAccess();
        }
    }

    @Override
    public Object makeParticlePacket(
            String name,
            double x, double y, double z,
            float dx, float dy, float dz,
            float speed,
            int amount,
            Material material,
            int data) throws Exception {
        if (particleAccess == null) {
            particleAccess = newParticleAccess();
        }
        return particleAccess.make(name, x, y, z, dx, dy, dz, speed, amount, material, data);
    }

    @Override
    public void sendPackets(Player player, Iterable<?> packets) throws Exception {
        if (particleAccess == null) {
            particleAccess = newParticleAccess();
        }
        particleAccess.send(player, packets);
    }

    // Factory methods are the extension points for later generations.

    protected PlayerPacketInjector newPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        return new LegacyPacketInjector(plugin, dispatcher);
    }

    protected KillerAccess newKillerAccess() {
        return new KillerAccess();
    }

    protected CollisionAccess newCollisionAccess() {
        return new CollisionAccess();
    }

    protected ActionBarAccess newActionBarAccess() {
        return new ActionBarAccess();
    }

    protected TitleAccess newTitleAccess() {
        return new TitleAccess();
    }

    protected DamageLoreAccess newDamageLoreAccess() {
        return new DamageLoreAccess();
    }

    protected ParticleAccess newParticleAccess() {
        return new ParticleAccess();
    }

    /**
     * Fallback used when the bounding box query is unavailable. Scanning loaded
     * chunks is coarse but never throws, which keeps projectiles functional on
     * unusual cores.
     */
    protected static List<LivingEntity> getNearbyByChunks(Location loc, double radius) {
        List<LivingEntity> list = new ArrayList<LivingEntity>();
        if (loc.getWorld() == null) {
            return list;
        }
        int minX = (int) Math.floor(loc.getX() - radius) >> 4;
        int maxX = (int) Math.floor(loc.getX() + radius) >> 4;
        int minZ = (int) Math.floor(loc.getZ() - radius) >> 4;
        int maxZ = (int) Math.floor(loc.getZ() + radius) >> 4;
        double radiusSq = radius * radius;
        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {
                for (Entity entity : loc.getWorld().getChunkAt(i, j).getEntities()) {
                    if (entity instanceof LivingEntity
                            && loc.distanceSquared(entity.getLocation()) < radiusSq) {
                        list.add((LivingEntity) entity);
                    }
                }
            }
        }
        return list;
    }
}

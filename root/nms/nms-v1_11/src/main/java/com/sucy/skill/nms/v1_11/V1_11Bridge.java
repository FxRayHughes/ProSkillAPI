package com.sucy.skill.nms.v1_11;

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
 * 1.11 世代的桥接实现。
 *
 * <p>1.11 起物品的攻击力 lore 与标题数据包结构变化，改用本模块自带的实现。</p>
 *
 * <p>本类刻意不继承其他版本模块：每个世代自带完整实现，
 * 改动一个版本不会静默影响另一个。</p>
 */
public class V1_11Bridge implements NmsBridge {
    private KillerAccess killerAccess;
    private CollisionAccess collisionAccess;
    private ActionBarAccess actionBarAccess;
    private TitleAccess titleAccess;
    private DamageLoreAccess damageLoreAccess;
    private ParticleAccess particleAccess;

    @Override
    public String id() {
        return "v1_11";
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
        return new MainHandPacketInjector(plugin, dispatcher);
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

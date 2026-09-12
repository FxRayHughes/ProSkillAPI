package com.sucy.skill.nms.v26_2;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 26.2 世代的桥接实现。
 *
 * <p>本类刻意不继承任何其他版本模块：每个世代自带完整实现，改动一个版本
 * 不会静默影响另一个。26.x 起 CraftBukkit 不再有版本段
 * （{@code org.bukkit.craftbukkit.entity.CraftPlayer}），26.2 与 26.1
 * 无法靠包名区分，因此各自绑定对应的服务端 jar 编译。</p>
 *
 * <p>这一代所有文本与物品接口都已是 Bukkit 公开 API，无需触碰 NMS 内部；
 * 唯一没有公开替代的是击杀者归属，调用方回落到插件自己维护的元数据。</p>
 */
public class V26_2Bridge implements NmsBridge {

    @Override
    public String id() {
        return "v26_2";
    }

    @Override
    public PlayerPacketInjector createPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        // 现代核心每个小版本都会混淆数据包类名，组合键改由 Bukkit 的交互与丢弃事件驱动。
        return NoOpPacketInjector.INSTANCE;
    }

    @Override
    public boolean markKiller(LivingEntity entity, Player player) {
        // 无公开 API 可写入原版的击杀者字段，交由调用方走元数据。
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
        // 26.x 的 Player 自带 sendActionBar，无需反射探测。
        return true;
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        try {
            player.sendActionBar(message);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        try {
            player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
        } catch (Throwable ignored) {
            // 标题只是观感，不能因此打断技能。
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
        // 粒子走 Player#spawnParticle，不需要预热数据包缓存。
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
        // 无包可发：粒子由封装层直接生成。
    }

    /**
     * 仅在当前核心确实声明了该标记时添加。ItemFlag 的常量在各版本间有增有减。
     */
    private static void addFlag(ItemMeta meta, String name) {
        if (!NmsCapabilities.hasEnumConstant(ItemFlag.class, name)) {
            return;
        }
        try {
            meta.addItemFlags(ItemFlag.valueOf(name));
        } catch (Throwable ignored) {
            // 核心拒绝某个已声明的标记时，保证物品仍可用。
        }
    }

    /**
     * 维持注入器契约，但不触碰 Netty 内部。
     */
    private static final class NoOpPacketInjector implements PlayerPacketInjector {
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

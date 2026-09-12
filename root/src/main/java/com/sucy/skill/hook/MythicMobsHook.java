package com.sucy.skill.hook;

import com.sucy.skill.hook.mythic.MythicProvider;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * SkillAPI © 2017
 * com.sucy.skill.hook.MythicMobsHook
 * <p>
 * MythicMobs 4 与 5 的包名完全不同，本类只作门面，实际调用委托给运行时选出的
 * {@link MythicProvider}。主工程因此不在编译期接触任何 MythicMobs 类型。
 */
public class MythicMobsHook {

    /** 各版本实现的全限定名，按优先级探测。 */
    private static final String[][] CANDIDATES = {
            {"io.lumine.mythic.bukkit.MythicBukkit", "com.sucy.skill.hook.mythic.v5.MythicV5Provider"},
            {"io.lumine.xikage.mythicmobs.MythicMobs", "com.sucy.skill.hook.mythic.v4.MythicV4Provider"}
    };

    private static MythicProvider provider;
    private static boolean resolved;

    /**
     * 按类存在性探测当前服务器上的 MythicMobs 大版本并实例化对应实现。
     * <p>
     * 用反射实例化是必要的：直接 new 会让 JVM 在加载本类时解析实现类引用的
     * MythicMobs 类型，在缺失或版本不符的服务器上直接抛 NoClassDefFoundError。
     */
    public static synchronized void init() {
        if (resolved) return;
        resolved = true;

        for (String[] candidate : CANDIDATES) {
            if (!classExists(candidate[0])) continue;
            try {
                provider = (MythicProvider) Class.forName(candidate[1]).newInstance();
                return;
            } catch (Throwable ex) {
                Bukkit.getLogger().warning("[SkillAPI] MythicMobs 适配加载失败: " + ex);
            }
        }
    }

    private static boolean classExists(final String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * @return 当前生效的适配实现；未安装 MythicMobs 时为 null
     */
    public static MythicProvider getProvider() {
        init();
        return provider;
    }

    /**
     * @return 已装载的 MythicMobs 大版本（4 或 5），未安装时为 0
     */
    public static int getMajorVersion() {
        final MythicProvider p = getProvider();
        return p == null ? 0 : p.getMajorVersion();
    }

    public static void taunt(final LivingEntity target, final LivingEntity source, final double amount) {
        final MythicProvider p = getProvider();
        if (p != null) p.taunt(target, source, amount);
    }

    public static boolean isMonster(final LivingEntity target) {
        final MythicProvider p = getProvider();
        return p != null && p.isMonster(target);
    }

    public static boolean hasThreatTable(final LivingEntity entity) {
        final MythicProvider p = getProvider();
        return p != null && p.hasThreatTable(entity);
    }

    public static void addThreatToMM(final LivingEntity mob, final LivingEntity target, final double amount) {
        final MythicProvider p = getProvider();
        if (p != null) p.addThreatToMM(mob, target, amount);
    }

    public static boolean hasActiveTarget(final LivingEntity entity) {
        final MythicProvider p = getProvider();
        return p != null && p.hasActiveTarget(entity);
    }

    public static void clearThreatTable(final LivingEntity entity) {
        final MythicProvider p = getProvider();
        if (p != null) p.clearThreatTable(entity);
    }

    public static void shuffleThreatTable(final LivingEntity entity) {
        final MythicProvider p = getProvider();
        if (p != null) p.shuffleThreatTable(entity);
    }

    public static boolean castSkill(LivingEntity caster, String skillName) {
        final MythicProvider p = getProvider();
        return p != null && p.castSkill(caster, skillName);
    }

    public static void castSkill(LivingEntity caster, String skillName, Float power) {
        final MythicProvider p = getProvider();
        if (p != null) p.castSkill(caster, skillName, power);
    }

    public static void castSkill(LivingEntity caster, String skillName, Collection<Entity> targets, Float power) {
        final MythicProvider p = getProvider();
        if (p != null) p.castSkill(caster, skillName, targets, power);
    }

    public static List<String> getMobAttributes(final Entity entity) {
        final MythicProvider p = getProvider();
        return p == null ? Collections.<String>emptyList() : p.getMobAttributes(entity);
    }

    /**
     * 按键名读 MythicMobs 怪物配置里的一个数值字段；未安装 MythicMobs 时返回 0。
     */
    public static double getMobAttribute(final LivingEntity entity, final String attrName) {
        final MythicProvider p = getProvider();
        return p == null ? 0 : p.getMobAttribute(entity, attrName);
    }

    /**
     * 注册当前版本专属的监听器（自定义机制加载、怪物生成）。
     */
    public static void registerListeners(final Plugin plugin) {
        final MythicProvider p = getProvider();
        if (p != null) p.registerListeners(plugin);
    }

    /**
     * 取玩家视线前方的第一个生物。纯 Bukkit 实现，不依赖 MythicMobs。
     */
    public static LivingEntity getTargetedEntity(LivingEntity player) {
        final int range = 32;
        List<Entity> ne;
        if (Bukkit.isPrimaryThread()) {
            ne = player.getNearbyEntities(range, range, range);
        } else {
            // getNearbyEntities 只能在主线程调用，异步场景下切回主线程等待结果。
            try {
                final Future<List<Entity>> future = Bukkit.getScheduler().callSyncMethod(
                        Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("SkillAPI")),
                        new Callable<List<Entity>>() {
                            @Override
                            public List<Entity> call() {
                                return player.getNearbyEntities(range, range, range);
                            }
                        });
                ne = future.get();
            } catch (ExecutionException | InterruptedException ex) {
                return null;
            }
        }

        List<LivingEntity> entities = new ArrayList<>();
        for (Entity o : ne) {
            if (o instanceof LivingEntity) {
                entities.add((LivingEntity) o);
            }
        }

        BlockIterator bi;
        try {
            bi = new BlockIterator(player, range);
        } catch (IllegalStateException ex) {
            return null;
        }

        while (bi.hasNext()) {
            Block b = bi.next();
            int bx = b.getX();
            int by = b.getY();
            int bz = b.getZ();
            Material material = b.getType();
            if (material != Material.BARRIER && (material.isOccluding() || material.isSolid())) {
                break;
            }

            for (final LivingEntity e : entities) {
                final double ex = e.getLocation().getX();
                final double ey = e.getLocation().getY();
                final double ez = e.getLocation().getZ();
                // 判定视线所经方块是否落在实体的包围盒范围内
                if (bx - 0.75 <= ex && ex <= bx + 1.75
                        && bz - 0.75 <= ez && ez <= bz + 1.75
                        && by - 1 <= ey && ey <= by + 2.5) {
                    if (!(e instanceof Player) || ((Player) e).getGameMode() != GameMode.CREATIVE) {
                        return e;
                    }
                }
            }
        }

        return null;
    }
}

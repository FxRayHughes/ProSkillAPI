package com.sucy.skill.combat.threat;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.hook.MythicMobsHook;
import com.sucy.skill.hook.PluginChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仇恨管理器
 * <p>
 * 职责：
 * 1. 维护每个 MM 怪物的仇恨表
 * 2. 接收伤害/治疗事件，转换为仇恨值
 * 3. 每秒选 Top1 写入怪物的 goalTarget
 * 4. 怪物脱战 N 秒后清表
 * <p>
 * 仇恨规则：
 * - 伤害仇恨 = damage × (1 + threat-power/100)
 * - 治疗仇恨 = healing × healMultiplier，仅对仇恨表里有 target、且距离 ≤ healRange 的怪物生效
 * - 嘲讽 = 强制设为 Top1 × 1.5 + 锁定 N 秒
 * <p>
 * 整套系统以 MythicMobs 为前提；未安装时所有入口静默 no-op。
 */
public class ThreatManager {

    // 怪物UUID → 仇恨数据
    private static final Map<UUID, MobThreatData> threats = new ConcurrentHashMap<>();

    /** 嘲讽时一次性写入 MM ThreatTable 的超大值，过期后原样减回。 */
    private static final double TAUNT_AMOUNT = 999999999.0;

    // 配置项（由 Settings 加载时注入）
    private static boolean enabled = true;
    private static double healRange = 25.0;
    private static double healMultiplier = 0.5;
    private static long combatTimeoutMs = 5000L;

    public static void configure(boolean enabled, double healRange, double healMultiplier, long combatTimeoutMs) {
        ThreatManager.enabled = enabled;
        ThreatManager.healRange = healRange;
        ThreatManager.healMultiplier = healMultiplier;
        ThreatManager.combatTimeoutMs = combatTimeoutMs;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static double getHealRange() {
        return healRange;
    }

    public static double getHealMultiplier() {
        return healMultiplier;
    }

    /**
     * 玩家对怪物造成伤害 → 添加仇恨
     * <p>
     * 双系统同步：
     * 1. SkillAPI 独立仇恨表（用于 threat-power 属性、治疗仇恨、目标选择器）
     * 2. MM ThreatTable（用于 MM AI 选目标，让怪物自然追高仇恨玩家）
     * <p>
     * MM 怪物配置需配合：
     * Options.UseThreatTable: true      ← 启用 MM ThreatTable，AI 基于它选目标
     * ThreatTable.UseDamageTaken: false ← 关！防止 MM 自己再加一次伤害仇恨造成双重计数
     */
    public static void addDamageThreat(LivingEntity mob, Player player, double damage) {
        if (!enabled || !PluginChecker.isMythicMobsActive()) return;
        if (mob == null || player == null || damage <= 0) return;
        if (!MythicMobsHook.isMonster(mob)) return;

        // 仇恨强度属性加成（SkillAPI 独有属性，必须在这里算好再传）
        double threatPower = getThreatPower(player);
        double threat = damage * (1.0 + threatPower / 100.0);
        getOrCreate(mob).addThreat(player.getUniqueId(), threat);

        // 把 SkillAPI 计算好的最终仇恨值同步写入 MM ThreatTable，
        // 这样 MM AI 会基于 threat-power 加成后的仇恨选目标（怪物会追坦克这类高仇恨职业）
        MythicMobsHook.addThreatToMM(mob, player, threat);
    }

    /**
     * 获取玩家的仇恨强度（threat-power）属性值，返回百分比加成。
     * <p>
     * scaleStat 以 100 为基准缩放：没有配置该属性时原样返回 100，此处即得 0 加成。
     */
    private static double getThreatPower(Player player) {
        try {
            PlayerData data = SkillAPI.getPlayerData(player);
            if (data == null) return 0;
            final double base = 100.0;
            double scaled = data.scaleStat("threat-power", base);
            return Math.max(0, scaled - base);
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * 治疗仇恨 - 给所有"仇恨表里有 target、且距离 healer ≤ healRange"的怪物添加 healer 仇恨
     * <p>
     * 同步写入 MM ThreatTable：让 MM 怪物也会追治疗者（网游常见机制：治疗会吸引怪物注意力）
     */
    public static void addHealThreat(Player healer, LivingEntity target, double healing) {
        if (!enabled || !PluginChecker.isMythicMobsActive()) return;
        if (healer == null || target == null || healing <= 0) return;

        UUID targetId = target.getUniqueId();
        double threat = healing * healMultiplier;
        double healRangeSq = healRange * healRange;

        for (MobThreatData data : threats.values()) {
            // 仇恨表里没有被治疗者 → 跳过
            if (!data.hasPlayer(targetId)) continue;

            LivingEntity mob = data.getMob();
            if (mob == null || mob.isDead()) continue;
            if (!mob.getWorld().equals(healer.getWorld())) continue;
            if (mob.getLocation().distanceSquared(healer.getLocation()) > healRangeSq) continue;

            data.addThreat(healer.getUniqueId(), threat);
            // 同步写入 MM ThreatTable：让 MM AI 也认治疗仇恨
            MythicMobsHook.addThreatToMM(mob, healer, threat);
        }
    }

    /**
     * 嘲讽 - 一次性向 MM ThreatTable 写入超大值，并设置定时器到期后减去。
     * <p>
     * 1. SkillAPI 侧：标记 tauntTarget=玩家，tauntExpire=到期时间
     * 2. MM ThreatTable 侧：一次性写入超大值 → MM AI 认该玩家为最高仇恨
     * 3. 设置定时器，taunt 结束时再去 MM ThreatTable 把超大值减掉
     * 4. 边缘情况（clearThreats/shuffleThreats/怪物死亡/玩家掉线）通过 tauntValid 标记处理
     * <p>
     * 这样 taunt 过期后 MM AI 会基于真实的伤害/治疗仇恨重新选目标，不会"永远追 taunt 玩家"。
     */
    public static void taunt(LivingEntity mob, Player player, long durationMs) {
        if (!enabled) return;
        if (mob == null || player == null) return;
        if (!PluginChecker.isMythicMobsActive() || !MythicMobsHook.isMonster(mob)) return;

        MobThreatData data = getOrCreate(mob);

        // 1. SkillAPI 侧：标记 tauntTarget、tauntExpire，同时记录写入量以便定时器减去
        data.setTaunt(player.getUniqueId(), durationMs, TAUNT_AMOUNT);

        // 2. MM ThreatTable 侧：一次性写超大值，MM AI 会立即把该玩家当成最高仇恨
        MythicMobsHook.addThreatToMM(mob, player, TAUNT_AMOUNT);

        // 3. 立即生效一次 setTarget，确保第一时间怪物转脸
        applyTarget(mob, player);

        // 4. 定时器：到期把超大值减回去。期间若表被清/打乱/怪物死亡，tauntValid 会置 false 而跳过
        final UUID mobUuid = mob.getUniqueId();
        final UUID playerUuid = player.getUniqueId();
        long ticks = Math.max(1L, durationMs / 50L); // 毫秒 → tick（50ms/tick），至少 1 tick
        try {
            Bukkit.getScheduler().runTaskLater(
                    SkillAPI.getPlugin(SkillAPI.class),
                    () -> {
                        MobThreatData d = threats.get(mobUuid);
                        if (d == null || !d.isTauntValid()) return;

                        LivingEntity realMob = d.getMob();
                        if (realMob == null || realMob.isDead()) {
                            d.invalidateTaunt();
                            return;
                        }

                        Player realPlayer = Bukkit.getPlayer(playerUuid);
                        if (realPlayer == null || !realPlayer.isOnline() || realPlayer.isDead()) {
                            d.invalidateTaunt();
                            return;
                        }

                        // 一切正常 → 减去超大值（传负数即为减少），让 MM 基于真实仇恨重选目标
                        MythicMobsHook.addThreatToMM(realMob, realPlayer, -d.getTauntThreatAmount());
                        d.invalidateTaunt();
                    },
                    ticks
            );
        } catch (Exception ignored) {
            // 定时器设置失败的降级：让 tick() 中自然过期处理
        }
    }

    /**
     * 清空怪物自身的 SkillAPI 仇恨表 + 同步清除 MM 的目标系统
     * 对 MM 怪物：调用 clearThreatTable() 彻底重置 AI 目标状态
     * 对普通怪物：调用 Creature.setTarget(null) 清除当前目标
     */
    public static void clearThreats(LivingEntity mob) {
        if (!enabled || mob == null) return;
        // 先标记 taunt 失效，否则定时器到期会去 MM 减那个超大值，而表马上就要被整个清空（会出负数）
        MobThreatData data = threats.get(mob.getUniqueId());
        if (data != null) {
            data.invalidateTaunt();
        }
        threats.remove(mob.getUniqueId());
        if (PluginChecker.isMythicMobsActive() && MythicMobsHook.isMonster(mob)) {
            // MM 怪物：必须同步重置 MM 自己的 AI 目标系统，
            // 否则仅清空 SkillAPI 仇恨表不足以让怪物停止追玩家
            MythicMobsHook.clearThreatTable(mob);
        } else if (mob instanceof Creature) {
            ((Creature) mob).setTarget(null);
        }
    }

    /**
     * 乱仇恨：将怪物现有仇恨表中玩家的仇恨值随机打乱
     * 对 MM 怪物：同时重置 MM AI 目标，产生乱仇恨效果
     */
    public static void shuffleThreats(LivingEntity mob) {
        if (!enabled || mob == null) return;
        MobThreatData data = threats.get(mob.getUniqueId());
        if (data != null) {
            data.shuffleThreats();
        }
        if (PluginChecker.isMythicMobsActive() && MythicMobsHook.isMonster(mob)) {
            MythicMobsHook.shuffleThreatTable(mob);
        }
    }

    /**
     * 目标选择器接口：从怪物的仇恨表中选仇恨最高的玩家
     */
    public static Player getHighestThreatTarget(LivingEntity mob) {
        MobThreatData data = threats.get(mob.getUniqueId());
        if (data == null) return null;
        UUID id = data.getTopTarget();
        return id == null ? null : Bukkit.getPlayer(id);
    }

    public static Player getLowestThreatTarget(LivingEntity mob) {
        MobThreatData data = threats.get(mob.getUniqueId());
        if (data == null) return null;
        UUID id = data.getLowestTarget();
        return id == null ? null : Bukkit.getPlayer(id);
    }

    public static Player getRandomThreatTarget(LivingEntity mob) {
        MobThreatData data = threats.get(mob.getUniqueId());
        if (data == null) return null;
        UUID id = data.getRandomTarget();
        return id == null ? null : Bukkit.getPlayer(id);
    }

    public static List<Player> getAllThreatTargets(LivingEntity mob) {
        MobThreatData data = threats.get(mob.getUniqueId());
        if (data == null) return java.util.Collections.emptyList();
        List<Player> result = new ArrayList<>();
        for (UUID id : data.getAllTargets()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) result.add(p);
        }
        return result;
    }

    /**
     * 找怪物周围最近的玩家（作为仇恨表为空时的降级目标选择）
     */
    public static Player findNearestPlayer(LivingEntity mob) {
        if (mob == null || !mob.isValid()) return null;
        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == null || !p.isOnline() || p.isDead()) continue;
            if (!p.getWorld().equals(mob.getWorld())) continue;
            double d = p.getLocation().distanceSquared(mob.getLocation());
            if (d < nearestDistSq) {
                nearestDistSq = d;
                nearest = p;
            }
        }
        return nearest;
    }

    /**
     * 怪物死亡/卸载 → 清理仇恨表
     */
    public static void removeMob(UUID mobId) {
        threats.remove(mobId);
    }

    /**
     * 玩家退出 → 从所有仇恨表移除
     */
    public static void removePlayer(UUID playerId) {
        for (MobThreatData data : threats.values()) {
            data.removePlayer(playerId);
        }
    }

    /**
     * 主循环 tick - 每秒执行
     * 1. 清理过期（脱战）的怪物 → 整个怪物的仇恨表丢弃
     * 2. 逐玩家检查超时 → 某玩家超时没获得新仇恨 → 仅从该怪物表中移除该玩家
     * 3. 给每个怪物设置 Top1 为 goalTarget
     * <p>
     * 采用纯时间戳检测而非 MM 内部状态：玩家被拉回刷怪点 / 死亡 / 脱离范围 → 停止攻击 → 超时自动清空
     */
    public static void tick() {
        if (!enabled) return;
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, MobThreatData>> it = threats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, MobThreatData> entry = it.next();
            MobThreatData data = entry.getValue();

            // 1. 怪物整体超时：太久没人攻击 → 整张表丢弃，同步清除 MM ThreatTable
            if (now - data.getLastUpdate() > combatTimeoutMs) {
                // 先失效 taunt，否则定时器到期会去减超大值，而 MM 表马上要被清空
                data.invalidateTaunt();
                it.remove();
                LivingEntity mob = data.getMob();
                if (mob != null && PluginChecker.isMythicMobsActive() && MythicMobsHook.isMonster(mob)) {
                    MythicMobsHook.clearThreatTable(mob);
                }
                continue;
            }

            // 直接从弱引用拿怪物，无须扫描世界
            LivingEntity mob = data.getMob();
            if (mob == null || mob.isDead() || !mob.isValid()) {
                data.invalidateTaunt(); // 怪物死了 → taunt 失效，防止定时器到期去 MM 减
                it.remove();
                continue;
            }

            // 2. 逐玩家超时检测（伤害、治疗、嘲讽都会更新时间戳）
            if (data.tickPlayerTimeout(combatTimeoutMs)) {
                data.invalidateTaunt();
                it.remove();
                if (PluginChecker.isMythicMobsActive() && MythicMobsHook.isMonster(mob)) {
                    MythicMobsHook.clearThreatTable(mob);
                }
                continue;
            }

            // 3. 选 Top1 作为目标
            UUID topId = data.getTopTarget();
            if (topId == null) continue;
            Player target = Bukkit.getPlayer(topId);
            if (target == null || !target.isOnline() || target.isDead()) {
                data.removePlayer(topId);
                continue;
            }
            if (!target.getWorld().equals(mob.getWorld())) {
                data.removePlayer(topId);
                continue;
            }

            // taunt 锁定期间只做 Creature.setTarget，不再往 MM ThreatTable 写入：
            // taunt() 已一次性写入超大值，每秒累加会导致过期后 MM 表残留巨量仇恨
            applyTarget(mob, target);
        }
    }

    /**
     * 内部：把怪物的 goalTarget 设为指定玩家
     */
    private static void applyTarget(LivingEntity mob, Player target) {
        if (!(mob instanceof Creature)) return;
        try {
            Creature creature = (Creature) mob;
            if (creature.getTarget() != target) {
                creature.setTarget(target);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取或创建仇恨数据，并缓存实体引用
     */
    private static MobThreatData getOrCreate(LivingEntity mob) {
        MobThreatData data = threats.computeIfAbsent(mob.getUniqueId(), k -> new MobThreatData());
        data.setMob(mob);
        return data;
    }

    /**
     * 清理所有数据
     */
    public static void clearAll() {
        threats.clear();
    }
}

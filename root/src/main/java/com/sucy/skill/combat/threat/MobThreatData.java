package com.sucy.skill.combat.threat;

import org.bukkit.entity.LivingEntity;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * 单个怪物的仇恨数据
 * 维护玩家→仇恨值的映射，以及嘲讽锁定信息
 */
public class MobThreatData {

    // 玩家UUID → 累计仇恨值
    private final Map<UUID, Double> scores = new HashMap<>();
    // 玩家UUID → 最后一次获得仇恨的时间（玩家级超时检测）
    private final Map<UUID, Long> playerTimes = new HashMap<>();
    // 最后一次仇恨更新时间（怪物级超时检测）
    private long lastUpdate;
    // 嘲讽锁定（强制目标）
    private UUID tauntTarget;
    private long tauntExpire;
    // taunt 时写入 MM ThreatTable 的超大值（过期时要减去它，防止 taunt 玩家永远在仇恨表顶端）
    private double tauntThreatAmount;
    // taunt 是否仍然有效（被 clearThreats/shuffleThreats/怪物死亡时会置 false，定时器就不用再减了）
    private boolean tauntValid;
    // 弱引用缓存的怪物实体（避免每秒全世界 UUID 扫描）
    private WeakReference<LivingEntity> mobRef;

    public MobThreatData() {
        this.lastUpdate = System.currentTimeMillis();
    }

    public void setMob(LivingEntity mob) {
        if (mob != null && (this.mobRef == null || this.mobRef.get() != mob)) {
            this.mobRef = new WeakReference<>(mob);
        }
    }

    public LivingEntity getMob() {
        return this.mobRef == null ? null : this.mobRef.get();
    }

    /**
     * 添加仇恨（伤害/治疗/嘲讽都会调用），同时更新玩家时间戳
     */
    public void addThreat(UUID playerId, double amount) {
        long now = System.currentTimeMillis();
        scores.merge(playerId, amount, Double::sum);
        playerTimes.put(playerId, now);
        lastUpdate = now;
    }

    public boolean hasPlayer(UUID playerId) {
        return scores.containsKey(playerId) && scores.get(playerId) > 0;
    }

    /**
     * 逐玩家检查超时：移除超过 combatTimeoutMs 没有获得新仇恨的玩家
     *
     * @return true=整个表已空，可以丢弃
     */
    public boolean tickPlayerTimeout(long combatTimeoutMs) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Double>> it = scores.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Double> entry = it.next();
            UUID playerId = entry.getKey();
            Long lastTime = playerTimes.get(playerId);
            // 超过超时阈值 → 从该怪物的仇恨表中移除该玩家
            if (lastTime == null || now - lastTime > combatTimeoutMs) {
                it.remove();
                playerTimes.remove(playerId);
                if (playerId.equals(tauntTarget)) {
                    tauntTarget = null;
                }
            }
        }
        return scores.isEmpty();
    }

    public UUID getTopTarget() {
        // 嘲讽锁定中
        if (tauntTarget != null && System.currentTimeMillis() < tauntExpire) {
            return tauntTarget;
        }
        tauntTarget = null;

        // 找仇恨最高的玩家
        UUID top = null;
        double topScore = 0;
        for (Map.Entry<UUID, Double> entry : scores.entrySet()) {
            if (entry.getValue() > topScore) {
                topScore = entry.getValue();
                top = entry.getKey();
            }
        }
        return top;
    }

    /**
     * 获取仇恨最低的玩家（仅返回有 &gt;0 仇恨的玩家）
     */
    public UUID getLowestTarget() {
        UUID lowest = null;
        double lowestScore = Double.MAX_VALUE;
        for (Map.Entry<UUID, Double> entry : scores.entrySet()) {
            double v = entry.getValue();
            if (v > 0 && v < lowestScore) {
                lowestScore = v;
                lowest = entry.getKey();
            }
        }
        return lowest;
    }

    /**
     * 随机选一个有仇恨的玩家
     */
    public UUID getRandomTarget() {
        List<UUID> valid = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : scores.entrySet()) {
            if (entry.getValue() > 0) valid.add(entry.getKey());
        }
        if (valid.isEmpty()) return null;
        return valid.get((int) (Math.random() * valid.size()));
    }

    /**
     * 获取所有仇恨玩家（用于目标选择器返回多个目标）
     */
    public List<UUID> getAllTargets() {
        List<UUID> list = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : scores.entrySet()) {
            if (entry.getValue() > 0) list.add(entry.getKey());
        }
        return list;
    }

    public double getTopScore() {
        double topScore = 0;
        for (double v : scores.values()) {
            if (v > topScore) topScore = v;
        }
        return topScore;
    }

    /**
     * 检测当前是否处于嘲讽锁定状态（有 tauntTarget 且未过期）
     */
    public boolean isTauntActive() {
        return tauntTarget != null && System.currentTimeMillis() < tauntExpire;
    }

    /**
     * 乱仇恨：将现有玩家的仇恨值随机打乱（不改变玩家列表，只交换仇恨值）
     * 用于 MM 怪物施放「乱仇恨」技能
     */
    public void shuffleThreats() {
        if (scores.size() < 2) return;
        long now = System.currentTimeMillis();
        List<UUID> players = new ArrayList<>(scores.keySet());
        List<Double> values = new ArrayList<>(scores.values());
        // Fisher-Yates 打乱
        for (int i = values.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            double tmp = values.get(i);
            values.set(i, values.get(j));
            values.set(j, tmp);
        }
        scores.clear();
        for (int i = 0; i < players.size(); i++) {
            scores.put(players.get(i), values.get(i));
            playerTimes.put(players.get(i), now);
        }
        invalidateTaunt(); // 乱仇恨后 taunt 失效，同时通知 MM 侧不要减那个超大值了
        lastUpdate = now;
    }

    /**
     * 设置 taunt 锁定。同时记录 MM 侧写入的超大值，过期时减去它。
     *
     * @param playerId          被 taunt 的玩家 UUID
     * @param durationMs        taunt 持续时间（毫秒）
     * @param tauntThreatAmount taunt 时一次性写入 MM ThreatTable 的超大值（SkillAPI 负责写入，
     *                          过期时通过 MythicMobsHook.addThreatToMM 传入负值来"减去"）
     */
    public void setTaunt(UUID playerId, long durationMs, double tauntThreatAmount) {
        long now = System.currentTimeMillis();
        double topScore = getTopScore();
        double tauntScore = Math.max(topScore * 1.5, 100);
        scores.put(playerId, tauntScore);
        playerTimes.put(playerId, now);
        this.tauntTarget = playerId;
        this.tauntExpire = now + durationMs;
        this.tauntThreatAmount = tauntThreatAmount; // 记录写入 MM 的超大值
        this.tauntValid = true;                     // 标记 taunt 有效，定时器可以安全减
        this.lastUpdate = now;
    }

    /**
     * 标记 taunt 失效（MM 侧仇恨值已经被改变，比如 clearThreats/shuffleThreats/怪物死亡）
     * 这样到期的定时器就不会再去 MM ThreatTable 减那个超大值了，避免负数
     */
    public void invalidateTaunt() {
        this.tauntValid = false;
        this.tauntTarget = null;
    }

    /**
     * 获取 taunt 时写入 MM 的超大值（仅当 tauntValid=true 时才用）
     */
    public double getTauntThreatAmount() {
        return tauntThreatAmount;
    }

    /**
     * taunt 是否仍然有效（定时器到期时调用：true=去 MM 减那个超大值；false=什么也不做）
     */
    public boolean isTauntValid() {
        return tauntValid;
    }

    public void removePlayer(UUID playerId) {
        scores.remove(playerId);
        playerTimes.remove(playerId);
        if (playerId.equals(tauntTarget)) {
            tauntTarget = null;
        }
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public boolean isEmpty() {
        return scores.isEmpty();
    }

    public void clear() {
        scores.clear();
        playerTimes.clear();
        invalidateTaunt(); // 清空整张表时 taunt 自然失效
    }
}

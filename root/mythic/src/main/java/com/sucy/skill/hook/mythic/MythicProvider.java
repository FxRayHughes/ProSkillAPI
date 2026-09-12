package com.sucy.skill.hook.mythic;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.List;

/**
 * MythicMobs 适配层的统一入口。
 * <p>
 * MythicMobs 4 与 5 的包名完全不同（{@code io.lumine.xikage.mythicmobs} 对
 * {@code io.lumine.mythic}），因此每个大版本各有一个实现模块，运行时按类存在性选取。
 * 本接口刻意只使用 Bukkit 类型，使主工程无需在编译期接触任何 MythicMobs 类。
 */
public interface MythicProvider {

    /** 这个实现对应的 MythicMobs 大版本，仅用于日志。 */
    int getMajorVersion();

    boolean isMonster(LivingEntity target);

    /**
     * 按正负增减仇恨；MythicMobs 侧的写入接口只认正数，因此负值走 reduce。
     */
    void taunt(LivingEntity target, LivingEntity source, double amount);

    boolean hasThreatTable(LivingEntity entity);

    void addThreatToMM(LivingEntity mob, LivingEntity target, double amount);

    boolean hasActiveTarget(LivingEntity entity);

    void clearThreatTable(LivingEntity entity);

    void shuffleThreatTable(LivingEntity entity);

    boolean castSkill(LivingEntity caster, String skillName);

    void castSkill(LivingEntity caster, String skillName, Float power);

    void castSkill(LivingEntity caster, String skillName, Collection<Entity> targets, Float power);

    /**
     * 读取怪物配置里的 {@code psk-attribute} 列表；非 MythicMobs 实体返回空列表。
     */
    List<String> getMobAttributes(Entity entity);

    /**
     * 直接按键名读怪物配置里的一个数值字段。
     * <p>
     * 与 {@link #getMobAttributes(Entity)} 读固定的 {@code psk-attribute} 列表不同，
     * 这里把键名交给调用方，用于 MM 怪物配置里平铺书写的自定义属性，例如：
     * <pre>
     * SkeletonKing:
     *   Type: SKELETON
     *   暴击率: 30
     * </pre>
     *
     * @param entity   目标实体
     * @param attrName 配置里的键名
     * @return 键对应的数值；非 MythicMobs 实体、键不存在或读取出错时为 0
     */
    double getMobAttribute(LivingEntity entity, String attrName);

    /**
     * 注册本版本专属的 Bukkit 监听器（自定义机制加载、怪物生成），
     * 由主工程在 onEnable 时调用。
     *
     * @param plugin 注册监听器用的插件实例
     */
    void registerListeners(Object plugin);
}

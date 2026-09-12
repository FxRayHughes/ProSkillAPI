package com.sucy.skill.hook.mythic;

import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * 适配模块回调主工程的契约。
 * <p>
 * v4/v5 模块只依赖本接口层，不反向依赖主工程（否则构成循环依赖）。
 * 主工程在启动时通过 {@link MythicBridges#register} 注入实现。
 */
public interface SkillBridge {

    /**
     * 让实体施放一个 SkillAPI 技能。
     *
     * @return 是否成功施放
     */
    boolean cast(LivingEntity caster, String skillName, int level);

    /**
     * 让实体施放一个 SkillAPI 动态技能，并把目标传给技能内部的目标选择器。
     * <p>
     * 目标以 {@code api-target} 为键写入施法者的 cast data，技能里用
     * {@code Remember} 目标选择器（key 填 {@code api-target}）即可取用。
     *
     * @param target 可为 null，表示无目标施放
     * @return 是否成功施放
     */
    boolean castAtTarget(LivingEntity caster, LivingEntity target, String skillName, int level);

    /**
     * 清空实体自身的仇恨表（MythicMobs 怪物会同步重置其 AI 目标）。
     */
    void clearThreats(LivingEntity entity);

    /**
     * 打乱实体仇恨表中各玩家的仇恨值。
     */
    void shuffleThreats(LivingEntity entity);

    /**
     * 以 SkillAPI 的伤害体系对目标造成伤害。
     *
     * @param classification 伤害分类，空串表示默认
     * @param trueDamage     true 表示无视减伤的真实伤害
     */
    void damage(LivingEntity target, double amount, LivingEntity source,
                String classification, boolean trueDamage);

    /**
     * 把怪物配置里的一条 {@code psk-attribute} 记到该实体的属性数据上。
     */
    void applyMobAttribute(UUID entityId, String attributeLine);
}

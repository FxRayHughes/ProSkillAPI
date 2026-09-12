package com.sucy.skill.listener;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.event.PhysicalDamageEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.hook.CitizensHook;
import com.sucy.skill.hook.MythicMobsHook;
import com.sucy.skill.hook.PluginChecker;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Random;

/**
 * 战斗属性监听器：处理暴击、闪避、吸血，并发送战斗消息。
 * <p>
 * 与伤害流程的配合方式（{@link AttributeListener} 在 HIGH 优先级做加伤减防）：
 * <ul>
 *     <li>LOW —— 掷骰判定暴击与闪避。闪避直接取消事件；暴击只打 meta 标记并暂存倍率，
 *     不当场改伤害，否则会被 HIGH 阶段的 physical-damage 加伤重新覆盖计算基数。</li>
 *     <li>HIGHEST —— 加伤减防都结束后再乘暴击倍率。</li>
 *     <li>MONITOR —— 基于最终伤害算吸血并发消息。</li>
 * </ul>
 * 技能伤害只有一个 LOW 阶段，因为 {@link SkillDamageEvent} 的暴击倍率与韧性可以一次算完。
 * <p>
 * 仇恨不在本监听器处理：{@link ThreatListener} 已在 MONITOR 把
 * {@link PhysicalDamageEvent}/{@link SkillDamageEvent} 的最终伤害喂给仇恨表。
 */
public class CombatListener extends SkillAPIListener {

    private static final Random RANDOM = new Random();

    /** 暴击/闪避标记的 meta key，供 critical / dodge 条件组件读取。 */
    public static final String META_CRIT  = "skillapi-last-crit";
    public static final String META_DODGE = "skillapi-last-dodge";

    // 物理伤害跨优先级传递用。同一个事件在同一线程内串行走完三个阶段，
    // 用 ThreadLocal 而非实例字段是为了避免不同线程的伤害事件互相踩数据。
    private static final ThreadLocal<boolean[]> PHYS_CRIT = new ThreadLocal<boolean[]>() {
        @Override
        protected boolean[] initialValue() {
            return new boolean[]{false};
        }
    };
    private static final ThreadLocal<double[]> PHYS_CRIT_MULT = new ThreadLocal<double[]>() {
        @Override
        protected double[] initialValue() {
            return new double[]{1.0};
        }
    };
    /** 标记 LOW 阶段是否真的跑过，避免被别的插件取消后仍然发消息。 */
    private static final ThreadLocal<boolean[]> PHYS_PROCESSED = new ThreadLocal<boolean[]>() {
        @Override
        protected boolean[] initialValue() {
            return new boolean[]{false};
        }
    };
    private static final ThreadLocal<boolean[]> SKILL_CRIT = new ThreadLocal<boolean[]>() {
        @Override
        protected boolean[] initialValue() {
            return new boolean[]{false};
        }
    };

    private String critRateAttr;
    private String critDamageAttr;
    private String dodgeAttr;
    private String lifestealAttr;
    private String tenacityAttr;

    private boolean combatMsgEnabled;
    private String  msgDealDamage;
    private String  msgDealSkillDamage;
    private String  msgTakeDamage;
    private String  msgTakeSkillDamage;
    private String  msgCritTag;
    private String  msgDodgeAttacker;
    private String  msgDodgeTarget;
    private String  msgLifestealTag;

    @Override
    public void init() {
        critRateAttr = SkillAPI.getSettings().getCombatAttrName("crit-rate", "暴击率");
        critDamageAttr = SkillAPI.getSettings().getCombatAttrName("crit-damage", "暴击伤害");
        dodgeAttr = SkillAPI.getSettings().getCombatAttrName("dodge", "闪避");
        lifestealAttr = SkillAPI.getSettings().getCombatAttrName("lifesteal", "吸血");
        tenacityAttr = SkillAPI.getSettings().getCombatAttrName("tenacity", "韧性");

        combatMsgEnabled = SkillAPI.getSettings().isCombatMsgEnabled();
        msgDealDamage = colorize(SkillAPI.getSettings().getMsgDealDamage());
        msgDealSkillDamage = colorize(SkillAPI.getSettings().getMsgDealSkillDamage());
        msgTakeDamage = colorize(SkillAPI.getSettings().getMsgTakeDamage());
        msgTakeSkillDamage = colorize(SkillAPI.getSettings().getMsgTakeSkillDamage());
        msgCritTag = colorize(SkillAPI.getSettings().getMsgCritTag());
        msgDodgeAttacker = colorize(SkillAPI.getSettings().getMsgDodgeAttacker());
        msgDodgeTarget = colorize(SkillAPI.getSettings().getMsgDodgeTarget());
        msgLifestealTag = colorize(SkillAPI.getSettings().getMsgLifestealTag());
    }

    @Override
    public void cleanup() {
    }

    private String colorize(String msg) {
        return msg == null ? "" : ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * 物理伤害的暴击/闪避判定。只打 meta 标记并暂存倍率，不在此处改伤害。
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.getDamage() <= 0) return;

        final LivingEntity damager = event.getDamager();
        if (damager == null) return;
        final LivingEntity target = event.getTarget();

        // 清掉上一次的判定结果，避免条件组件读到过期标记
        SkillAPI.removeMeta(damager, META_CRIT);
        SkillAPI.removeMeta(target, META_DODGE);

        boolean crit = false;
        double critMultiplier = 1.0;

        final double critRate = getAttributeValue(damager, "crit-rate");
        if (critRate > 0 && roll(critRate)) {
            final double critDamage = getAttributeValue(damager, "crit-damage");
            critMultiplier = SkillAPI.getSettings().getBaseCritDamage() + (critDamage / 100.0);
            crit = true;
            SkillAPI.setMeta(damager, META_CRIT, true);
        }

        final double dodgeRate = getAttributeValue(target, "dodge");
        if (dodgeRate > 0 && roll(dodgeRate)) {
            event.setCancelled(true);
            SkillAPI.setMeta(target, META_DODGE, true);
            sendDodgeMessage(damager, target);
            return;
        }

        PHYS_CRIT.get()[0] = crit;
        PHYS_CRIT_MULT.get()[0] = critMultiplier;
        PHYS_PROCESSED.get()[0] = true;
    }

    /**
     * 加伤减防结束后应用暴击倍率。目标韧性按百分比抵消暴击的“额外”部分：
     * {@code effective = 1 + (mult - 1) * (1 - min(1, tenacity / 100))}。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPhysicalDamageCritApply(PhysicalDamageEvent event) {
        if (!PHYS_CRIT.get()[0]) return;
        final double mult = PHYS_CRIT_MULT.get()[0];
        if (mult > 1.0) {
            event.setDamage(event.getDamage() * applyTenacity(event.getTarget(), mult));
        }
    }

    /**
     * 最终伤害确定后结算吸血并发消息。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhysicalDamageMessage(PhysicalDamageEvent event) {
        final boolean processed = PHYS_PROCESSED.get()[0];
        final boolean crit = PHYS_CRIT.get()[0];
        PHYS_CRIT.get()[0] = false;
        PHYS_CRIT_MULT.get()[0] = 1.0;
        PHYS_PROCESSED.get()[0] = false;

        // 没走过 LOW 阶段说明事件在更早的优先级被取消过，不该发消息
        if (!processed) return;
        if (event.getDamage() <= 0) return;

        final LivingEntity damager = event.getDamager();
        final double finalDamage = event.getDamage();
        final double lifestealHeal = applyLifesteal(damager, finalDamage);
        sendCombatMessage(damager, event.getTarget(), finalDamage, null, crit, lifestealHeal);
    }

    /**
     * 技能伤害的暴击/闪避判定。倍率与韧性在此一次算完，减防仍由
     * {@link AttributeListener#onSkillDamage} 在 HIGH 阶段处理。
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSkillDamage(SkillDamageEvent event) {
        if (event.getDamage() <= 0) return;

        final LivingEntity damager = event.getDamager();
        final LivingEntity target = event.getTarget();
        double damage = event.getDamage();
        boolean crit = false;

        SkillAPI.removeMeta(damager, META_CRIT);
        SkillAPI.removeMeta(target, META_DODGE);

        final double critRate = getAttributeValue(damager, "crit-rate");
        if (critRate > 0 && roll(critRate)) {
            final double critDamage = getAttributeValue(damager, "crit-damage");
            final double multiplier = SkillAPI.getSettings().getBaseCritDamage() + (critDamage / 100.0);
            damage = damage * applyTenacity(target, multiplier);
            crit = true;
            SkillAPI.setMeta(damager, META_CRIT, true);
        }

        final double dodgeRate = getAttributeValue(target, "dodge");
        if (dodgeRate > 0 && roll(dodgeRate)) {
            event.setCancelled(true);
            SkillAPI.setMeta(target, META_DODGE, true);
            sendDodgeMessage(damager, target);
            return;
        }

        event.setDamage(damage);
        SKILL_CRIT.get()[0] = crit;
    }

    /**
     * 技能伤害减防结束后结算吸血并发消息。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSkillDamageMessage(SkillDamageEvent event) {
        final boolean crit = SKILL_CRIT.get()[0];
        SKILL_CRIT.get()[0] = false;

        if (event.isCancelled()) return;
        if (event.getDamage() <= 0) return;

        final LivingEntity damager = event.getDamager();
        final double finalDamage = event.getDamage();
        final double lifestealHeal = applyLifesteal(damager, finalDamage);
        final String skillName = event.getSkill() == null ? null : event.getSkill().getName();
        sendCombatMessage(damager, event.getTarget(), finalDamage, skillName, crit, lifestealHeal);
    }

    /**
     * 掷骰。rate 是百分比值，超过 100 视为必定触发。
     */
    private boolean roll(final double rate) {
        return RANDOM.nextDouble() < Math.min(1.0, rate / 100.0);
    }

    /**
     * 用目标韧性削减暴击倍率高于 1 的部分。韧性 100 时暴击等同于普通伤害。
     */
    private double applyTenacity(final LivingEntity target, final double multiplier) {
        final double tenacity = getAttributeValue(target, "tenacity");
        final double reduce = Math.min(1.0, Math.max(0.0, tenacity / 100.0));
        return 1.0 + (multiplier - 1.0) * (1.0 - reduce);
    }

    /**
     * 按吸血属性回血，返回实际回复量（0 表示没吸到血）。
     */
    private double applyLifesteal(final LivingEntity damager, final double finalDamage) {
        if (damager == null || damager.isDead()) return 0;
        final double lifestealRate = getAttributeValue(damager, "lifesteal");
        if (lifestealRate <= 0) return 0;

        final double healAmount = finalDamage * (lifestealRate / 100.0);
        if (healAmount <= 0) return 0;

        final double max = damager.getMaxHealth();
        final double newHealth = Math.min(damager.getHealth() + healAmount, max);
        if (newHealth <= damager.getHealth()) return 0;
        damager.setHealth(newHealth);
        return healAmount;
    }

    /**
     * 反算实体的战斗属性百分比值。
     * <p>
     * 战斗属性没有独立存储，而是借 attributes.yml 的公式表达：把基准值 100 交给
     * {@link AttributeAPI#scaleStat} 缩放，再按 {@code Combat.calc-mode} 反算出偏移量。
     * {@code AttributeAPI} 内部已经同时覆盖玩家与非玩家（MythicMobs 怪物走
     * {@code MobAttribute} 存储），因此这里不需要分别处理两类实体。
     *
     * @param entity  目标实体，Citizens NPC 与 null 一律按 0 处理
     * @param statKey 战斗属性统计 key，如 {@code crit-rate}
     * @return 百分比值，30 表示 30%；未配置对应属性时为 0
     */
    public static double getAttributeValue(final LivingEntity entity, final String statKey) {
        if (entity == null) return 0;
        if (entity instanceof Player && CitizensHook.isNPC(entity)) return 0;

        final double base = 100.0;
        final double scaled = AttributeAPI.scaleStat(entity, statKey, base);
        final String mode = SkillAPI.getSettings().getCombatCalcMode(statKey);
        // additive 公式（a+v）缩放后大于基准，inverse 公式（v/(a*0.01+1)）缩放后小于基准
        final double effective = "additive".equalsIgnoreCase(mode) ? scaled - base : base - scaled;
        return Math.max(0, effective);
    }

    /**
     * 取实体的展示名，MythicMobs 怪物优先用它自己的 CustomName。
     */
    private String getEntityName(final LivingEntity entity) {
        if (entity == null) return "";
        if (entity instanceof Player) return ((Player) entity).getDisplayName();
        if (PluginChecker.isMythicMobsActive()) {
            try {
                if (MythicMobsHook.isMonster(entity)) {
                    final String mmName = entity.getCustomName();
                    if (mmName != null && !mmName.isEmpty()) return mmName;
                }
            } catch (Exception ignored) {
            }
        }
        final String customName = entity.getCustomName();
        return customName == null || customName.isEmpty() ? entity.getName() : customName;
    }

    /**
     * 给攻击者与受击者分别发伤害消息，双方都是玩家时各收一条。
     *
     * @param skillName 技能名，null 表示普通攻击
     */
    private void sendCombatMessage(
            final LivingEntity attacker,
            final LivingEntity target,
            final double damage,
            final String skillName,
            final boolean crit,
            final double lifestealHeal) {

        if (!combatMsgEnabled) return;
        if (!(attacker instanceof Player) && !(target instanceof Player)) return;

        final String dmgStr = String.format("%.1f", damage);
        final String attackerName = getEntityName(attacker);
        final String targetName = getEntityName(target);

        String tags = "";
        if (crit) tags += msgCritTag;
        if (lifestealHeal > 0) tags += msgLifestealTag.replace("{heal}", String.format("%.1f", lifestealHeal));

        if (attacker instanceof Player) {
            final String msg = skillName == null
                    ? msgDealDamage.replace("{target}", targetName).replace("{damage}", dmgStr)
                    : msgDealSkillDamage.replace("{skill}", skillName)
                            .replace("{target}", targetName)
                            .replace("{damage}", dmgStr);
            ((Player) attacker).sendMessage(msg + tags);
        }

        if (target instanceof Player) {
            final String msg = skillName == null
                    ? msgTakeDamage.replace("{attacker}", attackerName).replace("{damage}", dmgStr)
                    : msgTakeSkillDamage.replace("{attacker}", attackerName)
                            .replace("{skill}", skillName)
                            .replace("{damage}", dmgStr);
            ((Player) target).sendMessage(msg + tags);
        }
    }

    /**
     * 闪避成功时给双方发消息。
     */
    private void sendDodgeMessage(final LivingEntity attacker, final LivingEntity target) {
        if (!combatMsgEnabled) return;
        if (attacker instanceof Player) {
            ((Player) attacker).sendMessage(msgDodgeAttacker.replace("{target}", getEntityName(target)));
        }
        if (target instanceof Player) {
            ((Player) target).sendMessage(msgDodgeTarget.replace("{attacker}", getEntityName(attacker)));
        }
    }
}

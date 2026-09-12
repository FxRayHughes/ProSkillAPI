package com.sucy.skill.listener;

import com.sucy.skill.api.event.PhysicalDamageEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.event.SkillHealEvent;
import com.sucy.skill.api.event.TrueDamageEvent;
import com.sucy.skill.combat.threat.ThreatManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 把战斗事件喂给 {@link ThreatManager}。
 * <p>
 * 仇恨表只对 MythicMobs 怪物有意义，ThreatManager 的每个入口都自带
 * MythicMobs 可用性判断，因此未安装 MythicMobs 时这些回调全部是空操作。
 * <p>
 * 用 MONITOR 优先级读取最终伤害值：此时其他插件的增伤/减伤都已结算完毕。
 */
public class ThreatListener extends SkillAPIListener {

    /**
     * 普通攻击造成的伤害计入仇恨
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhysical(PhysicalDamageEvent event) {
        addThreat(event.getDamager(), event.getTarget(), event.getDamage());
    }

    /**
     * 技能伤害计入仇恨
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillDamage(SkillDamageEvent event) {
        addThreat(event.getDamager(), event.getTarget(), event.getDamage());
    }

    /**
     * 真实伤害同样计入仇恨，否则无视防御的技能会完全不拉仇恨
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrueDamage(TrueDamageEvent event) {
        addThreat(event.getDamager(), event.getTarget(), event.getDamage());
    }

    /**
     * 治疗产生仇恨：给正在仇恨被治疗者、且在范围内的怪物添加治疗者的仇恨
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(SkillHealEvent event) {
        final LivingEntity healer = event.getHealer();
        if (!(healer instanceof Player)) return;
        ThreatManager.addHealThreat((Player) healer, event.getTarget(), event.getAmount());
    }

    /**
     * 怪物死亡 → 丢弃它的仇恨表
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        ThreatManager.removeMob(event.getEntity().getUniqueId());
    }

    /**
     * 玩家退出 → 从所有怪物的仇恨表中移除
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        ThreatManager.removePlayer(event.getPlayer().getUniqueId());
    }

    private void addThreat(final LivingEntity damager, final LivingEntity target, final double damage) {
        if (!(damager instanceof Player)) return;
        ThreatManager.addDamageThreat(target, (Player) damager, damage);
    }
}

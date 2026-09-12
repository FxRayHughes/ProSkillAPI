package com.sucy.skill.dynamic.condition;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.listener.CombatListener;
import org.bukkit.entity.LivingEntity;

/**
 * 要求目标最近一次受击触发了闪避。
 */
@SkillNode(
        key = "dodge",
        name = "Dodge",
        nameZh = "检查闪避",
        description = "Applies child components only to targets whose most recent hit taken was dodged.",
        descriptionZh = "逐个读取目标身上的闪避标记，只放行标记存在的目标。"
                + "标记由战斗监听器在闪避掷骰成功时写入，同时那一次伤害事件会被取消，"
                + "每次伤害开始判定前会先清除上一次的标记。"
                + "闪避成功意味着伤害事件已被取消，因此挂在“受到物理伤害”“受到技能伤害”下不会执行；"
                + "需要靠“攻击”“被攻击”等不依赖伤害结算的触发器来读取。",
        container = true)
public class DodgeCondition extends ConditionComponent {

    @Override
    public String getKey() {
        return "dodge";
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (target == null) return false;
        final Object meta = SkillAPI.getMeta(target, CombatListener.META_DODGE);
        return meta instanceof Boolean && (Boolean) meta;
    }
}

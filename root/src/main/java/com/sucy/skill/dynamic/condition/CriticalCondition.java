package com.sucy.skill.dynamic.condition;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.listener.CombatListener;
import org.bukkit.entity.LivingEntity;

/**
 * 要求施法者最近一次攻击触发了暴击。
 */
@SkillNode(
        key = "critical",
        name = "Critical",
        nameZh = "检查暴击",
        description = "Applies child components only when the caster's most recent attack was a critical hit.",
        descriptionZh = "读取施法者身上的暴击标记：只有本次伤害的暴击判定通过时标记才存在，"
                + "因此条件通过即表示这一击是暴击。标记由战斗监听器在暴击掷骰成功时写入，"
                + "每次伤害开始判定前会先清除上一次的标记。"
                + "必须挂在“物理伤害”或“技能伤害”触发器下，否则读到的是上一次战斗的残留结果或什么都读不到。"
                + "判定与目标无关，所有目标要么全部通过要么全部不通过。",
        container = true)
public class CriticalCondition extends ConditionComponent {

    @Override
    public String getKey() {
        return "critical";
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (caster == null) return false;
        final Object meta = SkillAPI.getMeta(caster, CombatListener.META_CRIT);
        return meta instanceof Boolean && (Boolean) meta;
    }
}

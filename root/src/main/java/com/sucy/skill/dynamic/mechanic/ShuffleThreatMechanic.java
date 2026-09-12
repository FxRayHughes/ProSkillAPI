package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.combat.threat.ThreatManager;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * 乱仇恨机制
 * <p>
 * 在技能编辑器中配置：
 * 先用目标选择器选中一个或多个怪物（如 SingleTarget / AreaTarget），
 * 再挂本机制打乱这些怪物仇恨表中玩家的仇恨值。
 * <p>
 * 适用场景：
 * - 群体乱仇恨：对范围内所有怪物同时乱仇恨，使它们重新选择目标
 */
@SkillNode(
        key = "shuffle threat",
        name = "Shuffle Threat",
        nameZh = "打乱仇恨",
        requiresPlugins = {"MythicMobs"},
        descriptionZh = "把目标怪物仇恨表中各玩家的仇恨值随机重排（玩家名单不变，只是数值互换位置），使怪物重新选择攻击对象，用于制造混乱或打破固定坦克节奏。需要 MythicMobs。跳过为 null 或已死亡的目标；只要有一个目标被处理就返回 true。本节点无任何可配置项。")
public class ShuffleThreatMechanic extends CustomEffectComponent {

    @Override
    public String getKey() {
        return "shuffle threat";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.MECHANIC;
    }

    @Override
    public String getDisplayName() {
        return "乱仇恨";
    }

    @Override
    public String getDescription() {
        return "将目标怪物的仇恨表中玩家的仇恨值随机打乱（玩家列表不变，仇恨值重新分配）。适合制造混乱战斗场面或打破固定坦克节奏。";
    }

    @Override
    public List<EditorOption> getOptions() {
        return new ArrayList<>();
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        boolean worked = false;
        for (LivingEntity target : targets) {
            if (target == null || target.isDead()) continue;
            ThreatManager.shuffleThreats(target);
            worked = true;
        }
        return worked;
    }
}

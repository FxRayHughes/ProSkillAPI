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
 * 清空仇恨机制
 * <p>
 * 在技能编辑器中配置：
 * 先用目标选择器选中一个或多个怪物（如 SingleTarget），再挂本机制清空其仇恨表。
 * <p>
 * 适用场景：
 * - 坦克救场：队友 OT 后清空怪物仇恨让坦克重新建立仇恨
 * - 隐身相关技能
 */
@SkillNode(
        key = "clear threat",
        name = "Clear Threat",
        nameZh = "清空仇恨",
        requiresPlugins = {"MythicMobs"},
        descriptionZh = "清空目标怪物自身的仇恨表，使其丢失当前仇恨目标，玩家需重新攻击才会重新建立仇恨。内部按 MythicMobs 是否可用分流：Mythic 生物同时清掉 Mythic 内部仇恨表，普通生物退化为 setTarget(null)。死亡或为空的目标被跳过；一个都没处理到时返回 false。本节点无任何可配置项，需要服务端装有 MythicMobs。")
public class ClearThreatMechanic extends CustomEffectComponent {

    @Override
    public String getKey() {
        return "clear threat";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.MECHANIC;
    }

    @Override
    public String getDisplayName() {
        return "清空仇恨";
    }

    @Override
    public String getDescription() {
        return "清空目标怪物自身的仇恨表，使其失去对玩家的仇恨值。玩家需重新攻击才能再次建立仇恨。同时清空 MythicMobs 内部仇恨表。";
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
            // clearThreats 内部已按 MythicMobs 是否可用分流，并对普通生物走 setTarget(null)
            ThreatManager.clearThreats(target);
            worked = true;
        }
        return worked;
    }
}

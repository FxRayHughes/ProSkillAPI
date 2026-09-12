package com.sucy.skill.dynamic.target;

import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.combat.threat.ThreatManager;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomComponent;
import com.sucy.skill.dynamic.custom.EditorOption;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * 从目标怪物的仇恨表中选择"仇恨最高"的玩家
 * <p>
 * 在技能编辑器中配置：
 * 先用目标选择器选中一个怪物（例如 single），再在其子节点中使用本组件，
 * 输出该怪物仇恨表中仇恨最高的玩家。
 * <p>
 * 典型用途：
 * - 坦克嘲讽类技能：从怪物的仇恨表中锁定仇恨最高的人做目标
 * - 治疗辅助：自动选择怪物首要目标进行保护
 */
@SkillNode(
        key = "threat highest",
        name = "Threat Highest",
        nameZh = "仇恨最高目标",
        container = true,
        requiresPlugins = {"MythicMobs"},
        descriptionZh = "把输入的每个怪物换成它仇恨表中仇恨值最高的在线玩家（仇恨必须大于 0）；若该怪物正处于嘲讽锁定期间，则固定返回嘲讽者。依赖 MythicMobs 的仇恨系统，未安装 MythicMobs 或仇恨功能关闭时仇恨表始终为空，本节点返回空列表、分支不执行。已死亡的输入实体会被跳过，且结果不经过阵营、穿墙、最多目标数等通用过滤。")
public class ThreatHighestTarget extends TargetComponent implements CustomComponent {

    @Override
    public String getKey() {
        return "threat highest";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TARGET;
    }

    @Override
    public String getDisplayName() {
        return "仇恨最高的玩家";
    }

    @Override
    public String getDescription() {
        return "从输入怪物的仇恨表中选择仇恨最高的玩家。输入的目标必须是有仇恨表的怪物（例如 MythicMobs 怪物）。";
    }

    @Override
    public List<EditorOption> getOptions() {
        return new ArrayList<>();
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    List<LivingEntity> getTargets(LivingEntity caster, int level, List<LivingEntity> entities) {
        List<LivingEntity> targets = new ArrayList<>();
        for (LivingEntity entity : entities) {
            if (entity == null || entity.isDead()) continue;
            Player target = ThreatManager.getHighestThreatTarget(entity);
            if (target != null && target.isOnline()) {
                targets.add(target);
            }
        }
        return targets;
    }

    @Override
    void makeIndicators(List<IIndicator> list, Player player, LivingEntity entity, int level) {
        makeCircleIndicator(list, entity, 0);
    }
}

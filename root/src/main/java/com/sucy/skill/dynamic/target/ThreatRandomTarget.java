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
 * 从目标怪物的仇恨表中"随机"选择一名有仇恨的玩家
 * <p>
 * 典型用途：
 * - 混乱战斗：随机从怪物仇恨表中选择玩家，制造不可预测的战斗节奏
 * - 随机点名技能：类似 MM 的随机目标机制，用于 BOSS 随机点名
 */
@SkillNode(
        key = "threat random",
        name = "Threat Random",
        nameZh = "随机仇恨目标",
        container = true,
        requiresPlugins = {"MythicMobs"},
        descriptionZh = "把输入的每个怪物换成它仇恨表中随机一名在线玩家，候选仅限仇恨大于 0 的玩家，每次执行结果都可能不同，适合 BOSS 随机点名。依赖 MythicMobs 的仇恨系统，未安装 MythicMobs 或仇恨功能关闭时仇恨表始终为空，本节点返回空列表、分支不执行。已死亡的输入实体会被跳过，且结果不经过阵营、穿墙、最多目标数等通用过滤。")
public class ThreatRandomTarget extends TargetComponent implements CustomComponent {

    @Override
    public String getKey() {
        return "threat random";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TARGET;
    }

    @Override
    public String getDisplayName() {
        return "随机仇恨玩家";
    }

    @Override
    public String getDescription() {
        return "从输入怪物的仇恨表中随机选择一名有仇恨的玩家。输入的目标必须是有仇恨表的怪物（例如 MythicMobs 怪物）。";
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
            Player target = ThreatManager.getRandomThreatTarget(entity);
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

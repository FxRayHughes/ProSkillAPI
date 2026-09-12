package com.sucy.skill.dynamic.target;

import com.sucy.skill.api.util.Nearby;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomComponent;
import com.sucy.skill.dynamic.custom.EditorOption;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多目标最近选择器 - 选取范围内最近的N个目标（按距离排序）
 */
@SkillNode(
        key = "nearest multi",
        name = "Nearest Multi",
        nameZh = "最近多目标",
        description = "Targets the closest units in a radius from the current target, ordered from nearest to farthest.",
        descriptionZh = "以每个当前目标为球心，把半径内的生物按距离由近到远排序后交给通用过滤，取满「最多目标数」为止。"
                + "与 nearest 的区别：nearest 每个输入固定只出一个且不过滤，本节点会走阵营 / 穿墙 / 含施法者 / 最多目标数这套通用规则，"
                + "因此实际产出几个由「最多目标数」决定（该值未配置时默认 99，等于把半径内全部选中）。"
                + "排序的基准点是球心自身，半径内没有合法目标时返回空列表、子节点不执行。",
        container = true)
public class NearestMultiTarget extends TargetComponent implements CustomComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[radius] The radius of the area to target in blocks",
            tooltipZh = "搜索半径，单位方块；只有与球心直线距离在该值内的生物才进入候选，随后按距离升序排列。随技能等级缩放。",
            defaultValue = "3")
    private static final String RADIUS = "radius";

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        final double radius = parseValues(caster, RADIUS, level, 3.0);
        return determineTargets(caster, level, targets, source -> {
            final Location loc = source.getLocation();
            return Nearby.getLivingNearby(source, radius).stream()
                    .sorted(Comparator.comparingDouble(
                            (LivingEntity e) -> e.getLocation().distanceSquared(loc)))
                    .collect(Collectors.toList());
        });
    }

    /** {@inheritDoc} */
    @Override
    void makeIndicators(final List<IIndicator> list, final Player caster, final LivingEntity target, final int level) {
        makeCircleIndicator(list, target, parseValues(caster, RADIUS, level, 3.0));
    }

    @Override
    public String getKey() {
        return "nearest multi";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TARGET;
    }

    @Override
    public String getDisplayName() {
        return "最近多目标";
    }

    @Override
    public String getDescription() {
        return "从施法者周围选取范围内最近的N个目标（按距离由近到远排序）。";
    }

    @Override
    public List<EditorOption> getOptions() {
        return new ArrayList<>();
    }

    @Override
    public boolean isContainer() {
        return true;
    }
}

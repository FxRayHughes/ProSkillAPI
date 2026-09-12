package com.sucy.skill.dynamic.condition;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "ceiling",
        name = "Ceiling",
        nameZh = "检查头顶空间",
        description = "Checks the height of the ceiling above each target",
        descriptionZh = "检查目标头顶空间。从目标头顶第 2 格起向上扫到第 distance-1 格，遇到实心方块即视为“有天花板”。“至少”为是时要求这段范围内没有实心方块（头顶开阔）才通过；为否时必须扫到天花板才通过。",
        container = true)
public class CeilingCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Distance",
            labelZh = "距离",
            tooltip = "[distance] How high to check for the ceiling",
            tooltipZh = "向上扫描的高度上限（不含该格本身），实际检查第 2 格到第 distance-1 格，所以填 3 及以下几乎扫不到任何方块。不填按 5 计算。")
    private static final String DISTANCE = "distance";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "At least",
            labelZh = "至少",
            tooltip = "[at-least] When true, the ceiling must be at least the give number of blocks high. If false, the ceiling must be lower than the given number of blocks",
            tooltipZh = "是=要求扫描范围内没有实心方块（头顶开阔）；否=要求扫到实心方块（头顶被挡）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String AT_LEAST = "at-least";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean atLeast = settings.getBool(AT_LEAST, true);
        final int distance = (int) parseValues(caster, DISTANCE, level, 5);

        final Block block = target.getLocation().getBlock();
        boolean ceiling = false;
        for (int i = 2; i < distance; i++) {
            if (block.getRelative(0, i, 0).getType().isSolid()) {
                ceiling = true;
                break;
            }
        }
        return ceiling != atLeast;
    }

    @Override
    public String getKey() {
        return "ceiling";
    }
}

package com.sucy.skill.dynamic.condition;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "food",
        name = "Food",
        nameZh = "检查饥饿度",
        description = "Applies child components when the target's food level matches the settings.",
        descriptionZh = "要求目标是玩家，并按饥饿度过滤：可比较绝对值、占满值（20）的百分比，或与施法者的差值/百分比差值，结果落在 [最小值, 最大值] 闭区间内才通过。用 Difference 类型时会把施法者也当成玩家读取，施法者不是玩家会抛异常。",
        container = true)
public class FoodCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of measurement to use for the food. Food level is their flat food left. Percent is the percentage of food they have left. Difference is the difference between the target's flat food and the caster's. Difference percent is the difference between the target's percentage food left and the casters",
            tooltipZh = "Food=饥饿度绝对值；Percent=占满值 20 的百分比；Difference=目标减施法者的饥饿度；Difference Percent=两者百分比之差。比较忽略大小写，未匹配到时按 Food 处理。",
            options = {"Food", "Percent", "Difference", "Difference Percent"},
            optionsZh = {"饥饿度", "可选值2", "可选值3", "可选值4"},
            defaultValue = "Food")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Value",
            labelZh = "最小值",
            tooltip = "[min-value] The minimum food required. A positive minimum with one of the \"Difference\" types would be for when the target has more food",
            tooltipZh = "下限，闭区间。不填按 0 计算；配合 Difference 用正数即要求目标饥饿度高于施法者。")
    private static final String MIN  = "min-value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Value",
            labelZh = "最大值",
            tooltip = "[max-value] The maximum food required. A negative maximum with one of the \"Difference\" types would be for when the target has less food",
            tooltipZh = "上限，闭区间。不填按 999 计算；配合 Difference 用负数即要求目标饥饿度低于施法者。")
    private static final String MAX  = "max-value";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        if (!(target instanceof Player)) {
            return false;
        }

        // Older editor exports omit type for the default Food comparison;
        // default here so those valid YAML files remain loadable.
        final String type = settings.getString(TYPE, "Food").toLowerCase();
        final double min = parseValues(caster, MIN, level, 0);
        final double max = parseValues(caster, MAX, level, 999);

        double value;
        switch (type) {
            case "difference percent":
                value = (((Player) target).getFoodLevel() - ((Player) caster).getFoodLevel()) * 100 / ((Player) caster).getFoodLevel();
                break;
            case "difference":
                value = ((Player) target).getFoodLevel() - ((Player) caster).getFoodLevel();
                break;
            case "percent":
                value = ((Player) target).getFoodLevel() * 100 / 20;
                break;
            default:
                value = ((Player) target).getFoodLevel();
        }
        return value >= min && value <= max;
    }

    @Override
    public String getKey() {
        return "food";
    }
}

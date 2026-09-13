/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.HealthCondition
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.condition;

import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target's health to fit the requirement
 */
@SkillNode(
        key = "health",
        name = "Health",
        nameZh = "检查生命值",
        description = "Applies child components when the target's health matches the settings.",
        descriptionZh = "按目标生命值过滤：可比较绝对值、占自身最大生命的百分比，或与施法者的差值/百分比差值，结果落在 [最小值, 最大值] 闭区间内才通过。百分比差值用施法者当前生命作分母。",
        container = true)
public class HealthCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of measurement to use for the health. Health is their flat health left. Percent is the percentage of health they have left. Difference is the difference between the target's flat health and the caster's. Difference percent is the difference between the target's percentage health left and the casters",
            tooltipZh = "Health=生命值绝对值；Percent=占自身最大生命的百分比；Difference=目标减施法者的生命值；Difference Percent=以施法者当前生命为分母的百分比之差。比较忽略大小写，未匹配到时按 Health 处理。",
            options = {"Health", "Percent", "Difference", "Difference Percent"},
            optionsZh = {"生命值", "可选值2", "可选值3", "可选值4"},
            defaultValue = "Health")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Value",
            labelZh = "最小值",
            tooltip = "[min-value] The minimum health required. A positive minimum with one of the \"Difference\" types would be for when the target has more health",
            tooltipZh = "下限，闭区间。不填按 0 计算；配合 Difference 用正数即要求目标生命高于施法者。")
    private static final String MIN  = "min-value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Value",
            labelZh = "最大值",
            tooltip = "[max-value] The maximum health required. A negative maximum with one of the \"Difference\" types would be for when the target has less health",
            tooltipZh = "上限，闭区间。不填按 999 计算；配合 Difference 用负数即要求目标生命低于施法者。")
    private static final String MAX  = "max-value";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String type = settings.getString(TYPE, "Health").toLowerCase();
        final double min = parseValues(caster, MIN, level, 0);
        final double max = parseValues(caster, MAX, level, 999);

        double value;
        switch (type) {
            case "difference percent":
                value = (target.getHealth() - caster.getHealth()) * 100 / caster.getHealth();
                break;
            case "difference":
                value = target.getHealth() - caster.getHealth();
                break;
            case "percent":
                value = target.getHealth() * 100 / target.getMaxHealth();
                break;
            default:
                value = target.getHealth();
        }
        return value >= min && value <= max;
    }

    @Override
    public String getKey() {
        return "health";
    }
}

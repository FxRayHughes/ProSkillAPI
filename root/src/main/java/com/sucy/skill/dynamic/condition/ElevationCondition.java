/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ElevationCondition
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

import java.util.ArrayList;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to fit the elevation requirement
 */
@SkillNode(
        key = "elevation",
        name = "Elevation",
        nameZh = "检查高度差",
        description = "Applies child components when the elevation of the target matches the settings.",
        descriptionZh = "按高度过滤目标：类型为“Difference”时取“目标 Y 坐标减施法者 Y 坐标”（正数表示目标更高），其余情况直接取目标的 Y 坐标，落在 [最小值, 最大值] 闭区间内才通过。",
        container = true)
public class ElevationCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of comparison to make. Normal is just their Y-coordinate. Difference would be the difference between that the caster's Y-coordinate",
            tooltipZh = "Normal=直接用目标的 Y 坐标；Difference=用目标 Y 减施法者 Y 的差值（正数=目标更高，负数=目标更低）。比较忽略大小写，未匹配到时按 Normal 处理。",
            options = {"Normal", "Difference"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Normal")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Value",
            labelZh = "最小值",
            tooltip = "[min-value] The minimum value for the elevation required. A positive minimum value with a \"Difference\" type would be for when the target is higher up than the caster",
            tooltipZh = "高度下限，闭区间。不填按 0 计算；配合 Difference 用正数即要求目标高于施法者。")
    private static final String MIN  = "min-value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Value",
            labelZh = "最大值",
            tooltip = "[max-value] The maximum value for the elevation required. A negative maximum value with a \"Difference\" type would be for when the target is below the caster",
            tooltipZh = "高度上限，闭区间。不填按 255 计算；配合 Difference 用负数即要求目标低于施法者。")
    private static final String MAX  = "max-value";

    @Override
    public String getKey() {
        return "elevation";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String type = settings.getString(TYPE).toLowerCase();
        double min = parseValues(caster, MIN, level, 0);
        double max = parseValues(caster, MAX, level, 255);

        ArrayList<LivingEntity> list = new ArrayList<LivingEntity>();
        for (LivingEntity target : targets) {
            double value;
            if (type.equals("difference")) {
                value = target.getLocation().getY() - caster.getLocation().getY();
            } else {
                value = target.getLocation().getY();
            }
            if (value >= min && value <= max) {
                list.add(target);
            }
        }
        return list.size() > 0 && executeChildren(caster, level, list);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String type = settings.getString(TYPE);
        final double min = parseValues(caster, MIN, level, 0);
        final double max = parseValues(caster, MAX, level, 255);

        double value;
        if (type.equalsIgnoreCase("difference")) {
            value = target.getLocation().getY() - caster.getLocation().getY();
        } else {
            value = target.getLocation().getY();
        }
        return value >= min && value <= max;
    }
}

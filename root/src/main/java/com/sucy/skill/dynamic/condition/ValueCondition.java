/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ValueCondition
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

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "value",
        name = "Value",
        nameZh = "检查数值",
        description = "Applies child components if a stored value is within the given range.",
        descriptionZh = "读取施法者的施法数据（cast data）里指定键的数值，落在 [最小值, 最大值] 闭区间内才通过。该值必须先由数值类机制（如 Value Set 等）写入同一个键，键不存在时直接不通过；存的值必须是数字类型，否则会报错。该节点只判一次，通过后把原目标列表整体交给子节点。",
        container = true,
        requiresNodes = {"value set"})
public class ValueCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique string used for the value set by the Value mechanics.",
            tooltipZh = "施法数据的键名，需与写入数值的机制用的键一致；其中 {uuid} 会被替换为施法者的 UUID。键不存在则不通过。",
            defaultValue = "value")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Value",
            labelZh = "最小值",
            tooltip = "[min-value] The lower bound of the required value",
            tooltipZh = "下限，闭区间（值 >= 最小值）。不填按 1 计算。")
    private static final String MIN = "min-value";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max Value",
            labelZh = "最大值",
            tooltip = "[max-value] The upper bound of the required value",
            tooltipZh = "上限，闭区间（值 <= 最大值）。不填按 999 计算。")
    private static final String MAX = "max-value";

    @Override
    public String getKey() {
        return "value";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        return test(caster, level, null) && executeChildren(caster, level, targets);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        final double min = parseValues(caster, MIN, level, 1);
        final double max = parseValues(caster, MAX, level, 999);
        final Object data = DynamicSkill.getCastData(caster).get(key);

        if (data instanceof Number) {
            // Persisted/cast data can be Integer, Long or Double depending on
            // whether it originated in YAML, JSON, or a mechanic calculation.
            double value = ((Number) data).doubleValue();
            return value >= min && value <= max;
        }

        return false;
    }
}

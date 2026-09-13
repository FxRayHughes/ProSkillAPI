/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ValueCondition
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
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
import com.sucy.skill.dynamic.data.DataSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "data",
        name = "Data",
        nameZh = "检查数据",
        description = "判断Data在某个区间",
        descriptionZh = "读取按目标 UUID 存储的持久化数据值，落在 [最小值, 最大值] 闭区间内才通过。注意该节点不逐目标过滤：只用列表里第一个非空目标做判定，通过后把原始目标列表整体交给子节点；若列表为空或全为空则直接执行子节点（相当于不做检查）。",
        container = true)
public class DataCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] {uuid}会自动替换",
            tooltipZh = "数据键名，其中 {uuid} 会被替换为施法者的 UUID。键不存在时读到的值按 0 参与比较。",
            defaultValue = "标签ID")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min Value",
            labelZh = "最小值",
            tooltip = "[min-value] The lower bound of the required value",
            tooltipZh = "下限，闭区间（值 >= 最小值）。不填按 1 计算，支持随技能等级缩放。")
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
        return "data";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            if (target != null) {
                return test(caster, level, target) && executeChildren(caster, level, targets);
            }
        }
        return executeChildren(caster, level, targets);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        ;
        final double min = parseValues(caster, MIN, level, 1);
        final double max = parseValues(caster, MAX, level, 999);

        double value = DataSkill.getValue(target.getUniqueId(), key);
        return value >= min && value <= max;
    }
}

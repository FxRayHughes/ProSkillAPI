/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.AttributeCondition
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "attribute",
        name = "Attribute",
        nameZh = "检查属性",
        description = "Requires the target to have a given number of attributes",
        descriptionZh = "读取目标的属性点数，落在 [最小值, 最大值] 闭区间内才通过。属性名会转小写并去掉颜色符号后查询：玩家取自身分配与职业提供的总和，怪物取 Mob 属性数据；目标为空或已死亡时按 0 计算。",
        container = true)
public class AttributeCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Attribute",
            labelZh = "属性",
            tooltip = "[attribute] The name of the attribute you are checking the value of",
            tooltipZh = "要查询的属性名，大小写不敏感（内部转小写）。查不到该属性时按 0 参与比较。",
            defaultValue = "Vitality")
    private static final String ATTR = "attribute";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Min",
            labelZh = "最小值",
            tooltip = "[min] The minimum amount of the attribute the target requires",
            tooltipZh = "属性下限，闭区间（值 >= 最小值）。不填按 0 处理，支持随技能等级缩放。")
    private static final String MIN  = "min";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Max",
            labelZh = "最大值",
            tooltip = "[max] The maximum amount of the attribute the target requires",
            tooltipZh = "属性上限，闭区间（值 <= 最大值）。不填按 int 最大值处理，即不限上限。")
    private static final String MAX  = "max";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String attr = settings.getString(ATTR, null);
        final int min = (int) parseValues(caster, MIN, level, 0);
        final int max = (int) parseValues(caster, MAX, level, Integer.MAX_VALUE);

        final int value = AttributeAPI.getAttribute(target,attr);
        return value >= min && value <= max;
    }

    @Override
    public String getKey() {
        return "attribute";
    }
}

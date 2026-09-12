/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.ChanceCondition
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

import java.util.Random;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "chance",
        name = "Chance",
        nameZh = "检查概率",
        description = "Rolls a chance to apply child components.",
        descriptionZh = "对每个目标各自摇一次随机数决定是否通过。取 [0,1) 随机数与 概率/100 比较，严格小于才算命中，因此填 0 永不通过、填 100 及以上必定通过。不填按 25% 计算。",
        container = true)
public class ChanceCondition extends ConditionComponent
{
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Chance",
            labelZh = "概率",
            tooltip = "[chance] The chance to execute children as a percentage. \"25\" would be 25%.",
            tooltipZh = "百分比数值，25 表示 25%。判定为 随机数 < 概率/100，可随技能等级缩放。")
    private static final String CHANCE = "chance";
    private static final Random random = new Random();

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final double chance = parseValues(caster, CHANCE, level, 25) / 100.0;
        return random.nextDouble() < chance;
    }

    @Override
    public String getKey() {
        return "chance";
    }
}
